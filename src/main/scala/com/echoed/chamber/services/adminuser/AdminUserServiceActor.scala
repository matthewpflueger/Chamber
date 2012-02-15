package com.echoed.chamber.services.adminuser

import org.slf4j.LoggerFactory
import com.echoed.chamber.domain._
import akka.actor.{Channel, Actor}
import views.{RetailerProductSocialActivityByDate,RetailerSocialActivityByDate,RetailerCustomerSocialActivityByDate}
import com.echoed.chamber.dao.AdminUserDao
import com.echoed.chamber.dao.views.AdminViewDao
import scala.collection.JavaConversions._
import java.util.ArrayList

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
        adminViewDao: AdminViewDao) extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[AdminUserServiceActor])

    self.id = "AdminUser:%s" format adminUser.id

    def receive = {
        case msg: GetUsers =>
            logger.debug("Retrieving EchoedUsers")
            self.channel ! GetUsersResponse(msg,Right(asScalaBuffer(adminViewDao.getUsers).toList))
        case msg: GetEchoPossibilities =>
            logger.debug("Retrieving EchoPossibilities")
            self.channel ! GetEchoPossibilitesResponse(msg, Right(asScalaBuffer(adminViewDao.getEchoPossibilities).toList))
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
