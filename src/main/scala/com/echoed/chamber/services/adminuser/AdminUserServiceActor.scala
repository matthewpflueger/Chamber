package com.echoed.chamber.services.adminuser

import com.echoed.chamber.domain._
import com.echoed.chamber.dao.AdminUserDao
import com.echoed.chamber.dao.views.AdminViewDao
import scala.collection.JavaConversions._
import com.echoed.chamber.dao.partner.PartnerSettingsDao

import scalaz._
import Scalaz._
import akka.event.Logging
import akka.actor.{PoisonPill, Actor}


class AdminUserServiceActor(
        adminUser: AdminUser,
        adminUserDao: AdminUserDao,
        adminViewDao: AdminViewDao,
        partnerSettingsDao: PartnerSettingsDao) extends Actor {

    private final val logger = Logging(context.system, this)

    def receive = {
        case msg: GetUsers =>
            logger.debug("Retrieving EchoedUsers")
            sender ! GetUsersResponse(msg,Right(asScalaBuffer(adminViewDao.getUsers).toList))
        case msg: GetPartners =>
            logger.debug("Retrieving Partners")
            sender ! GetPartnersResponse(msg, Right(asScalaBuffer(adminViewDao.getPartners).toList))
        case msg @ GetPartnerSettings(partnerId) =>
            logger.debug("Retrieving Partner Settings for partner: {}", partnerId)
            sender ! GetPartnerSettingsResponse(msg, Right(asScalaBuffer(adminViewDao.getPartnerSettings(partnerId)).toList))
        case msg: GetEchoPossibilities =>
            logger.debug("Retrieving EchoPossibilities")
            sender ! GetEchoPossibilitesResponse(msg, Right(asScalaBuffer(adminViewDao.getEchoPossibilities).toList))
        case msg @ UpdatePartnerSettings(partnerSettings) =>
            val channel = sender
            logger.debug("Updating Partner Settings")
            Option(partnerSettingsDao.insert(partnerSettings)).cata(
                resultSet => {
                    logger.debug("Successfully inserted new Partner Settings")
                    channel ! UpdatePartnerSettingsResponse(msg, Right(partnerSettings))
                },
                {
                    logger.error("Error inserting new Partner Settings {}" , partnerSettings)
                    channel ! UpdatePartnerSettingsResponse(msg, Left(new AdminUserException("Error inserting PartnerSettings")))
                }
            )

        case msg: GetAdminUser =>
            logger.debug("Retreiving AdminUser: {}", adminUser)
            sender ! GetAdminUserResponse(msg, Right(adminUser))
        case msg @ Logout(adminUserId) =>
            val channel = sender

            try {
                assert(adminUser.id == adminUserId)
                channel ! LogoutResponse(msg, Right(true))
                self ! PoisonPill
                logger.debug("Logged out {}", adminUser)
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(AdminUserException("Could not logout", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }

        case _ =>
            logger.debug("No Standard Message")
            sender ! "None"

    }
}
