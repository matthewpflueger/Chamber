package com.echoed.chamber.services.partneruser

import akka.actor.Actor
import org.slf4j.LoggerFactory
import com.echoed.chamber.domain._
import scala.collection.mutable.{Map => MMap, ListBuffer => MList}
import com.echoed.chamber.dao.RetailerUserDao
import com.echoed.chamber.dao.views.RetailerViewDao


class PartnerUserServiceActor(
        partnerUser: RetailerUser,
        partnerUserDao: RetailerUserDao,
        retailerViewDao: RetailerViewDao) extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[PartnerUserServiceActor])

    self.id = "PartnerUser:%s" format partnerUser.id

    def receive = {
        case msg: GetPartnerUser =>
            self.channel ! GetPartnerUserResponse(msg, Right(partnerUser))

        case msg: GetRetailerSocialSummary =>
            self.channel ! GetRetailerSocialSummaryResponse(msg, Right(retailerViewDao.getSocialActivityByRetailerId(partnerUser.retailerId)))

        case msg: GetProductSocialSummary =>
            logger.debug("Getting Product Social Summary {} {}", msg.productId,partnerUser.retailerId)
            val resultSet = retailerViewDao.getSocialActivityByProductIdAndRetailerId(msg.productId, partnerUser.retailerId)
            logger.debug("Result Set: {}",resultSet)
            self.channel ! GetProductSocialSummaryResponse(msg, Right(resultSet))

        case msg: GetTopProducts =>
            self.channel ! GetTopProductsResponse(msg, Right(retailerViewDao.getTopProductsWithRetailerId(partnerUser.retailerId)))
    }

}
