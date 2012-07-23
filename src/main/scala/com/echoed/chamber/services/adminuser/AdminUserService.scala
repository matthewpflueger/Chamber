package com.echoed.chamber.services.adminuser

import com.echoed.chamber.domain._

import com.echoed.chamber.services.{EventProcessorActorSystem, MessageProcessor, OnlineOfflineService}
import com.echoed.chamber.services.state.{ReadAdminUserServiceStateResponse, ReadAdminUserServiceState}


class AdminUserService(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        adminUserId: Option[String] = None) extends OnlineOfflineService {

    private var adminUser: AdminUser = _


    override def preStart() {
        super.preStart()
        adminUserId.foreach(context.self ! ReadAdminUserServiceState(_))
    }

    def init = {
        case msg @ CreateAdminUser(aucc, adminUser) =>
            ep(AdminUserCreated(adminUser))
            context.sender ! CreateAdminUserResponse(msg, Right(adminUser))
            becomeOnline

        case ReadAdminUserServiceStateResponse(_, Right(au)) =>
            adminUser = au
            becomeOnline
    }

    def online = {
        case msg @ CreateAdminUser(aucc, adminUser) =>
            //FIXME check permissions, assign roles, etc and then copy the message inserting the updated AdminUser into it...
            context.parent ! CreateAdminUserService(msg, context.sender)

        case msg @ Login(email, password) =>
            //FIXME create new credential, invalidate existing credentials, etc and send back credential as response
            if (adminUser.isPassword(password)) sender ! LoginResponse(msg, Right(adminUser))
            else sender ! LoginResponse(msg, Left(LoginError("Invalid login")))
    }

/*
    def handle = {
        case msg: GetUsers =>
            log.debug("Retrieving EchoedUsers")
            sender ! GetUsersResponse(msg,Right(asScalaBuffer(adminViewDao.getUsers).toList))
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
        case msg @ Logout(adminUserId) =>
            val channel = sender

            try {
                assert(adminUser.id == adminUserId)
                channel ! LogoutResponse(msg, Right(true))
                self ! PoisonPill
                log.debug("Logged out {}", adminUser)
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(AdminUserException("Could not logout", e)))
                    log.error("Unexpected error processing %s" format msg, e)
            }

        case _ =>
            log.debug("No Standard Message")
            sender ! "None"

    }
 */
}
