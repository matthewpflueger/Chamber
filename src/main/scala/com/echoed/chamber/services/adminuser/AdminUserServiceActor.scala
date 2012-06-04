package com.echoed.chamber.services.adminuser

import org.slf4j.LoggerFactory
import com.echoed.chamber.domain._
import akka.actor.{Channel, Actor}
import views.{PartnerProductSocialActivityByDate,PartnerSocialActivityByDate,PartnerCustomerSocialActivityByDate}
import com.echoed.chamber.dao.AdminUserDao
import com.echoed.chamber.dao.views.AdminViewDao
import com.echoed.chamber.domain.partner.PartnerSettings
import scala.collection.JavaConversions._
import java.util.ArrayList
import com.echoed.chamber.dao.partner.PartnerSettingsDao


import scalaz._
import Scalaz._
/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/2/12
 * Time: 9:06 PM
 * To change this template use File | Settings | File Templates.
 */

class AdminUserServiceActor(
        adminUser: AdminUser,
        adminUserDao: AdminUserDao,
        adminViewDao: AdminViewDao,
        partnerSettingsDao: PartnerSettingsDao) extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[AdminUserServiceActor])

    self.id = "AdminUser:%s" format adminUser.id

    def receive = {
        case msg: GetUsers =>
            logger.debug("Retrieving EchoedUsers")
            self.channel ! GetUsersResponse(msg,Right(asScalaBuffer(adminViewDao.getUsers).toList))
        case msg: GetPartners =>
            logger.debug("Retrieving Partners")
            self.channel ! GetPartnersResponse(msg, Right(asScalaBuffer(adminViewDao.getPartners).toList))
        case msg @ GetPartnerSettings(partnerId) =>
            logger.debug("Retrieving Partner Settings for partner: {}", partnerId)
            self.channel ! GetPartnerSettingsResponse(msg, Right(asScalaBuffer(adminViewDao.getPartnerSettings(partnerId)).toList))
        case msg: GetEchoPossibilities =>
            logger.debug("Retrieving EchoPossibilities")
            self.channel ! GetEchoPossibilitesResponse(msg, Right(asScalaBuffer(adminViewDao.getEchoPossibilities).toList))
        case msg @ UpdatePartnerSettings(partnerSettings) =>
            val channel: Channel[UpdatePartnerSettingsResponse] = self.channel
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
            self.channel ! GetAdminUserResponse(msg, Right(adminUser))
        case msg @ Logout(adminUserId) =>
            val channel: Channel[LogoutResponse] = self.channel

            try {
                assert(adminUser.id == adminUserId)
                channel ! LogoutResponse(msg, Right(true))
                self.stop()
                logger.debug("Logged out {}", adminUser)
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(AdminUserException("Could not logout", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }
        case _ =>
            logger.debug("No Standard Message")
            self.channel ! "None"

    }
}
