package com.echoed.chamber.services.adminuser

import com.echoed.chamber.domain._
import com.echoed.chamber.dao.AdminUserDao
import com.echoed.chamber.dao.views.AdminViewDao
import scala.collection.JavaConversions._
import com.echoed.chamber.dao.partner.{PartnerDao, PartnerSettingsDao}

import scalaz._
import Scalaz._
import akka.event.{LoggingReceive, Logging}
import akka.actor.{PoisonPill, Actor}
import com.echoed.chamber.services.EchoedActor


class AdminUserServiceActor(
        adminUser: AdminUser,
        adminUserDao: AdminUserDao,
        adminViewDao: AdminViewDao,
        partnerSettingsDao: PartnerSettingsDao,
        partnerDao: PartnerDao) extends EchoedActor {

    def handle = {
        case msg: GetUsers =>
            log.debug("Retrieving EchoedUsers")
            sender ! GetUsersResponse(msg,Right(asScalaBuffer(adminViewDao.getUsers).toList))
        case msg: GetPartners =>
            log.debug("Retrieving Partners")
            sender ! GetPartnersResponse(msg, Right(asScalaBuffer(adminViewDao.getPartners).toList))
        case msg @ GetPartner(partnerId) =>
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

        case msg @ UpdatePartnerHandleAndCategory(partnerId, partnerHandle, partnerCategory) =>
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
        case msg @ GetPartnerSettings(partnerId) =>
            log.debug("Retrieving Partner Settings for partner: {}", partnerId)
            sender ! GetPartnerSettingsResponse(msg, Right(asScalaBuffer(adminViewDao.getPartnerSettings(partnerId)).toList))
        case msg @ GetCurrentPartnerSettings(partnerId) =>
            log.debug("Retreiving Current Partner Settings for Partner: {}", partnerId)
            sender ! GetCurrentPartnerSettingsResponse(msg, Right(adminViewDao.getCurrentPartnerSettings(partnerId)))
        case msg: GetEchoPossibilities =>
            log.debug("Retrieving EchoPossibilities")
            sender ! GetEchoPossibilitesResponse(msg, Right(asScalaBuffer(adminViewDao.getEchoPossibilities).toList))
        case msg @ UpdatePartnerSettings(partnerSettings) =>
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
}
