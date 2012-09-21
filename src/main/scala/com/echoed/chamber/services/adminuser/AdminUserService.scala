package com.echoed.chamber.services.adminuser


import com.echoed.chamber.services.{Message, EventProcessorActorSystem, MessageProcessor, OnlineOfflineService}
import com.echoed.chamber.services.state._
import com.echoed.chamber.dao.views.AdminViewDao
import scala.collection.JavaConversions._
import com.echoed.chamber.dao.partner.{PartnerSettingsDao, PartnerDao}
import scalaz._
import Scalaz._
import akka.actor.PoisonPill
import akka.pattern._
import scala.Left
import com.echoed.chamber.services.state.ReadAdminUserForEmail
import com.echoed.chamber.domain.AdminUser
import scala.Right
import com.echoed.chamber.services.state.ReadAdminUserForCredentials
import com.echoed.chamber.domain.partner.PartnerUser
import com.echoed.chamber.services.partneruser.{GetPartnerUserResponse, PartnerUserClientCredentials, GetPartnerUser}


class AdminUserService(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        initMessage: Message,
        adminViewDao: AdminViewDao,
        partnerDao: PartnerDao,
        partnerSettingsDao: PartnerSettingsDao) extends OnlineOfflineService {

    private var adminUser: AdminUser = _

    private def setStateAndRegister(au: AdminUser) {
        adminUser = au
        becomeOnline
        context.parent ! RegisterAdminUserService(adminUser)
    }

    override def preStart() {
        super.preStart()
        initMessage match {
            case LoginWithEmail(email, _, _) => mp(ReadAdminUserForEmail(email)).pipeTo(self)
            case LoginWithCredentials(credentials) => mp(ReadAdminUserForCredentials(credentials)).pipeTo(self)
        }
    }

    def init = {
        case msg @ ReadAdminUserForEmailResponse(_, Left(_)) => initMessage match {
            case LoginWithEmail(_, msg @ LoginWithEmailPassword(_, _), channel) =>
                channel.get ! LoginWithEmailPasswordResponse(msg, Left(InvalidCredentials())); self ! PoisonPill
        }

        case msg @ ReadAdminUserForEmailResponse(_, Right(au)) => setStateAndRegister(au)
        case msg @ ReadAdminUserForCredentialsResponse(_, Right(au)) => setStateAndRegister(au)
    }

    def online = {
        case msg @ LoginWithEmailPassword(email, password) =>
            //FIXME create new credential, invalidate existing credentials, etc and send back credential as response
            if (adminUser.isCredentials(email, password)) sender ! LoginWithEmailPasswordResponse(msg, Right(adminUser))
            else sender ! LoginWithEmailPasswordResponse(msg, Left(InvalidCredentials()))

        case msg @ BecomePartnerUser(_, partnerUserId) =>
            val channel = sender
            mp(GetPartnerUser(PartnerUserClientCredentials(partnerUserId))).onSuccess {
                case GetPartnerUserResponse(_, Right(pu)) => channel ! BecomePartnerUserResponse(msg, Right(pu))
            }

        case msg: GetUsers =>
            log.debug("Retrieving EchoedUsers")
            sender ! GetUsersResponse(msg, Right(asScalaBuffer(adminViewDao.getUsers).toList))


        case msg: GetPartners =>
            log.debug("Retrieving Partners")
            sender ! GetPartnersResponse(msg, Right(asScalaBuffer(adminViewDao.getPartners).toList))


        case msg @ GetPartner(aucc, partnerId) =>
            val channel = sender
            log.debug("Retrieving Partner {}", partnerId)
            Option(partnerDao.findById(partnerId)).cata(
                partner => {
                    log.debug("Successfully Retrieved Partner {} with PartnerId {}", partner, partnerId)
                    channel ! GetPartnerResponse(msg, Right(partner))
                },
                {
                    log.error("Error Retrieving Partner {}", partnerId)
                    channel ! GetPartnerResponse(msg, Left(new AdminUserException("Error Retrieving Partner")))
                })


        case msg @ UpdatePartnerHandleAndCategory(aucc, partnerId, partnerHandle, partnerCategory) =>
            val channel = sender
            log.debug("Updating Partner {}", partnerId)
            Option(partnerDao.updateHandleAndCategory(partnerId, partnerHandle, partnerCategory)).cata(
                resultSet => {
                    log.debug("Successfully updated Partner Handle and Category for Partner{}", partnerId)
                    channel ! UpdatePartnerHandleAndCategoryResponse(msg, Right(partnerHandle))
                },
                {
                    log.error("Error Updating Partner Handle and Category for Partner {}", partnerId)
                    channel ! UpdatePartnerHandleAndCategoryResponse(msg, Left(new AdminUserException("Error updating Partner Handle")))
                })


        case msg @ GetPartnerSettings(aucc, partnerId) =>
            log.debug("Retrieving Partner Settings for partner: {}", partnerId)
            sender ! GetPartnerSettingsResponse(msg, Right(asScalaBuffer(adminViewDao.getPartnerSettings(partnerId)).toList))


        case msg @ GetCurrentPartnerSettings(aucc, partnerId) =>
            log.debug("Retreiving Current Partner Settings for Partner: {}", partnerId)
            sender ! GetCurrentPartnerSettingsResponse(msg, Right(adminViewDao.getCurrentPartnerSettings(partnerId)))


        case msg: GetEchoPossibilities =>
            log.debug("Retrieving EchoPossibilities")
            sender ! GetEchoPossibilitiesResponse(msg, Right(asScalaBuffer(adminViewDao.getEchoPossibilities).toList))


        case msg @ UpdatePartnerSettings(aucc, partnerSettings) =>
            val channel = sender
            log.debug("Updating Partner Settings")
            Option(partnerSettingsDao.insert(partnerSettings)).cata(
                resultSet => {
                    log.debug("Successfully inserted new Partner Settings")
                    channel ! UpdatePartnerSettingsResponse(msg, Right(partnerSettings))
                },
                {
                    log.error("Error inserting new Partner Settings {}" , partnerSettings)
                    channel ! UpdatePartnerSettingsResponse(msg, Left(new AdminUserException("Error inserting PartnerSettings")))
                }
            )

        case msg: GetAdminUser =>
            log.debug("Retreiving AdminUser: {}", adminUser)
            sender ! GetAdminUserResponse(msg, Right(adminUser))


        case msg @ Logout(adminUserId) => self ! PoisonPill

    }

}
