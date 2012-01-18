package com.echoed.chamber.services.partneruser

import org.slf4j.LoggerFactory
import com.echoed.chamber.domain._
import com.echoed.chamber.dao.RetailerUserDao
import com.echoed.chamber.dao.views.RetailerViewDao
import akka.actor.{Channel, Actor}


class PartnerUserServiceActor(
        partnerUser: RetailerUser,
        partnerUserDao: RetailerUserDao,
        retailerViewDao: RetailerViewDao) extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[PartnerUserServiceActor])

    self.id = "PartnerUser:%s" format partnerUser.id

    def receive = {
        case msg @ Logout(partnerUserId) =>
            val channel: Channel[LogoutResponse] = self.channel

            try {
                assert(partnerUser.id == partnerUserId)
                channel ! LogoutResponse(msg, Right(true))
                self.stop
                logger.debug("Logged out {}", partnerUser)
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(PartnerUserException("Could not logout", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }

        case msg: GetPartnerUser =>
            self.channel ! GetPartnerUserResponse(msg, Right(partnerUser))

        case msg: GetRetailerSocialSummary =>
            self.channel ! GetRetailerSocialSummaryResponse(
                    msg,
                    Right(retailerViewDao.getSocialActivityByRetailerId(partnerUser.retailerId)))

        case msg: GetProductSocialSummary =>
            logger.debug("Getting Product Social Summary {} {}", msg.productId, partnerUser.retailerId)
            val resultSet = retailerViewDao.getSocialActivityByProductIdAndRetailerId(msg.productId, partnerUser.retailerId)
            logger.debug("Result Set: {}", resultSet)
            self.channel ! GetProductSocialSummaryResponse(msg, Right(resultSet))

        case msg: GetTopProducts =>
            self.channel ! GetTopProductsResponse(msg, Right(retailerViewDao.getTopProductsWithRetailerId(partnerUser.retailerId)))
    }

}
