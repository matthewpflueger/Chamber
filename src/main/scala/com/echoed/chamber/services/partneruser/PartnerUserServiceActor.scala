package com.echoed.chamber.services.partneruser

import org.slf4j.LoggerFactory
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.views.{SocialActivityTotalByDate}
import com.echoed.chamber.dao.RetailerUserDao
import com.echoed.chamber.dao.views.{RetailerViewDao}
import akka.actor.{Channel, Actor}
import java.util.ArrayList
import views.RetailerProductSocialActivityByDate
import views.RetailerSocialActivityByDate


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

        case msg: GetRetailerSocialActivityByDate =>
            var comments = retailerViewDao.getFacebookCommentsByRetailerIdDate(partnerUser.retailerId)
            if(comments.size == 1 && comments.get(0).count == 0)
                comments = new ArrayList[SocialActivityTotalByDate]
            var likes = retailerViewDao.getFacebookLikesByRetailerIdDate(partnerUser.retailerId)
            if(likes.size == 1 && likes.get(0).count == 0)
                likes = new ArrayList[SocialActivityTotalByDate]
            var echoClicks = retailerViewDao.getEchoClicksByRetailerIdDate(partnerUser.retailerId)
            if(echoClicks.size == 1 && echoClicks.get(0).count == 0)
                echoClicks = new ArrayList[SocialActivityTotalByDate]
            self.channel ! GetRetailerSocialActivityByDateResponse(msg, Right(new RetailerSocialActivityByDate(partnerUser.retailerId,likes,comments,echoClicks)))

        case msg: GetProductSocialSummary =>
            logger.debug("Getting Product Social Summary {} {}", msg.productId, partnerUser.retailerId)
            val comments = retailerViewDao.getTotalFacebookCommentsByRetailerIdProductId(msg.productId, partnerUser.retailerId)
            val likes = retailerViewDao.getTotalFacebookLikesByRetailerIdProductId(msg.productId, partnerUser.retailerId)
            val views = retailerViewDao.getTotalEchoClicksByRetailerIdProductId(msg.productId, partnerUser.retailerId)
            logger.debug("Comments {}",comments)
            val resultSet = retailerViewDao.getSocialActivityByProductIdAndRetailerId(msg.productId, partnerUser.retailerId)
            logger.debug("Result Set: {}", resultSet)
            self.channel ! GetProductSocialSummaryResponse(msg, Right(resultSet))

        case msg: GetProductSocialActivityByDate =>
            logger.debug("Getting Product Social Actvity By Date {}", msg productId)
            var comments = retailerViewDao.getFacebookCommentsByRetailerIdProductIdDate(msg.productId,partnerUser.retailerId)
            if(comments.size == 1 && comments.get(0).count == 0)
                comments = new ArrayList[SocialActivityTotalByDate]
            var likes = retailerViewDao.getFacebookLikesByRetailerIdProductIdDate(msg.productId, partnerUser.retailerId)
            if(likes.size == 1 && likes.get(0).count == 0)
                likes = new ArrayList[SocialActivityTotalByDate]
            var echoClicks = retailerViewDao.getEchoClicksByRetailerIdProductIdDate(msg.productId, partnerUser.retailerId)
            if(echoClicks.size == 1 && echoClicks.get(0).count == 0)
                echoClicks = new ArrayList[SocialActivityTotalByDate]
            self.channel ! GetProductSocialActivityByDateResponse(msg, Right(new RetailerProductSocialActivityByDate(partnerUser.retailerId,msg.productId,likes,comments,echoClicks)))


        case msg: GetProducts =>
            self.channel ! GetProductsResponse(msg, Right(retailerViewDao.getProductsWithRetailerId(partnerUser.retailerId,0,25)))

        case msg: GetTopProducts =>
            self.channel ! GetTopProductsResponse(msg, Right(retailerViewDao.getTopProductsWithRetailerId(partnerUser.retailerId)))

        case msg: GetCustomers =>
            self.channel ! GetCustomersResponse(msg, Right(retailerViewDao.getCustomersWithRetailerId(partnerUser.retailerId,0,25)))


        case msg: GetTopCustomers =>
            self.channel ! GetTopCustomersResponse(msg, Right(retailerViewDao.getTopCustomersWithRetailerId(partnerUser.retailerId)))
    }

}
