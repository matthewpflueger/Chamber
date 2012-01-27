package com.echoed.chamber.services.partneruser

import org.slf4j.LoggerFactory
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.views.{SocialActivityTotalByDate,CustomerSocialSummary}
import com.echoed.chamber.dao.RetailerUserDao
import com.echoed.chamber.dao.views.{RetailerViewDao}
import akka.actor.{Channel, Actor}
import scala.collection.JavaConversions._
import java.util.ArrayList
import views.{RetailerProductSocialActivityByDate,RetailerSocialActivityByDate,RetailerCustomerSocialActivityByDate}



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

        case msg: GetCustomerSocialSummary =>
            val echoedUser = retailerViewDao.getEchoedUserByRetailerUser(msg.echoedUserId,partnerUser.retailerId)
            if(echoedUser.id== null)
                self.channel ! GetCustomerSocialSummaryResponse(msg, Left(PartnerUserException("Error Retrieving Echoed User ")))
            else {
                val likes = retailerViewDao.getTotalLikesByEchoedUserRetailerId(msg.echoedUserId,partnerUser.retailerId)
                val comments  = retailerViewDao.getTotalCommentsByEchoedUserRetailerId(msg.echoedUserId,partnerUser.retailerId)
                val clicks = retailerViewDao.getTotalEchoClicksByEchoedUserRetailerId(msg.echoedUserId,partnerUser.retailerId)
                val echoes = retailerViewDao.getTotalEchoesByEchoedUserRetailerId(msg.echoedUserId,partnerUser.retailerId)
                val friends = retailerViewDao.getTotalFacebookFriendsByEchoedUser(msg.echoedUserId)
                self.channel ! GetCustomerSocialSummaryResponse(msg, Right(new CustomerSocialSummary(echoedUser.id,echoedUser.name,echoes,likes,comments,clicks,friends)))
            }

        case msg: GetCustomerSocialActivityByDate =>
            var likes = retailerViewDao.getFacebookLikesByRetailerIdCustomerIdDate(msg.echoedUserId,partnerUser.retailerId)
            if(likes.size == 1 && likes.get(0).count == 0)
                likes = new ArrayList[SocialActivityTotalByDate]
            var comments = retailerViewDao.getFacebookCommentsByRetailerIdCustomerIdDate(msg.echoedUserId,partnerUser.retailerId)
            if(comments.size == 1 && comments.get(0).count == 0)
                comments = new ArrayList[SocialActivityTotalByDate]
            var views = retailerViewDao.getEchoClicksByRetailerIdCustomerIdDate(msg.echoedUserId,partnerUser.retailerId)
            if(views.size == 1 && views.get(0).count == 0)
                views = new ArrayList[SocialActivityTotalByDate]
            self.channel ! GetCustomerSocialActivityByDateResponse(msg, Right(new RetailerCustomerSocialActivityByDate(partnerUser.retailerId,msg.echoedUserId,likes,comments,views)))

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

        case msg: GetComments =>
            val comments = asScalaBuffer(retailerViewDao.getCommentsByRetailerId(partnerUser.retailerId)).toList
            self.channel ! GetCommentsResponse(msg, Right(comments))

        case msg: GetCommentsByProductId =>
            val comments = asScalaBuffer(retailerViewDao.getCommentsByRetailerIdProductId(partnerUser.retailerId, msg.productId)).toList
            self.channel ! GetCommentsByProductIdResponse(msg, Right(comments))
    }

}
