package com.echoed.chamber.services.partneruser

import org.slf4j.LoggerFactory
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.views.{SocialActivityTotalByDate,CustomerSocialSummary,SocialActivityHistory}
import com.echoed.chamber.dao.RetailerUserDao
import com.echoed.chamber.dao.views.{RetailerViewDao}
import akka.actor.{Channel, Actor}
import scala.collection.JavaConversions._
import java.util.ArrayList
import views.{RetailerProductSocialActivityByDate,RetailerSocialActivityByDate,RetailerCustomerSocialActivityByDate}



class PartnerUserServiceActor(
        var partnerUser: RetailerUser,
        partnerUserDao: RetailerUserDao,
        retailerViewDao: RetailerViewDao) extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[PartnerUserServiceActor])

    self.id = "PartnerUser:%s" format partnerUser.id

    def receive = {
        case msg @ ActivatePartnerUser(password) =>
            val channel: Channel[ActivatePartnerUserResponse] = self.channel

            try {
                partnerUser = partnerUser.createPassword(password)
                partnerUserDao.updatePassword(partnerUser)
                channel ! ActivatePartnerUserResponse(msg, Right(partnerUser))
            } catch {
                case e: InvalidPassword =>
                    channel ! ActivatePartnerUserResponse(msg, Left(e))
                case e =>
                    channel ! ActivatePartnerUserResponse(
                            msg,
                            Left(PartnerUserException("Could not activate partner user %s" format partnerUser.name, e)))
            }

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
                val likes = retailerViewDao.getTotalFacebookLikes(partnerUser.retailerId, msg.echoedUserId, null)
                val comments  = retailerViewDao.getTotalFacebookComments(partnerUser.retailerId,msg.echoedUserId, null)
                val clicks = retailerViewDao.getTotalEchoClicks(partnerUser.retailerId,msg.echoedUserId, null)
                val echoes = retailerViewDao.getTotalEchoes(partnerUser.retailerId,msg.echoedUserId,null)
                val friends = retailerViewDao.getTotalFacebookFriendsByEchoedUser(msg.echoedUserId)
                val volume = retailerViewDao.getTotalSalesVolume(partnerUser.retailerId,msg.echoedUserId, null)
                val sales = retailerViewDao.getTotalSalesAmount(partnerUser.retailerId, msg.echoedUserId, null)
                self.channel ! GetCustomerSocialSummaryResponse(msg, Right(new CustomerSocialSummary(echoedUser.id,echoedUser.name,echoes,likes,comments,clicks,friends,sales,volume)))
            }

        case msg: GetCustomerSocialActivityByDate =>
            var series = new ArrayList[SocialActivityHistory]

            val likes = retailerViewDao.getFacebookLikesHistory(partnerUser.retailerId,msg.echoedUserId, null)
            series.add(new SocialActivityHistory("likes",likes))
            val comments = retailerViewDao.getFacebookLikesHistory(partnerUser.retailerId,msg.echoedUserId,null)
            series.add(new SocialActivityHistory("comments",comments))
            val views = retailerViewDao.getEchoClicksHistory(partnerUser.retailerId,msg.echoedUserId,null)
            series.add(new SocialActivityHistory("views",views))
            val purchases = retailerViewDao.getSalesVolumeHistory(partnerUser.retailerId,msg.echoedUserId, null)
            series.add(new SocialActivityHistory("Sales(#)",purchases))

            self.channel ! GetCustomerSocialActivityByDateResponse(msg, Right(new RetailerCustomerSocialActivityByDate(partnerUser.retailerId,msg.echoedUserId,series)))

        case msg: GetRetailerSocialSummary =>
            self.channel ! GetRetailerSocialSummaryResponse(
                    msg,
                    Right(retailerViewDao.getSocialActivityByRetailerId(partnerUser.retailerId)))

        case msg: GetRetailerSocialActivityByDate =>

            var series = new ArrayList[SocialActivityHistory]

            val comments = retailerViewDao.getFacebookCommentsHistory(partnerUser.retailerId,null,null)
            series.add(new SocialActivityHistory("comments",comments))
            val views = retailerViewDao.getEchoClicksHistory(partnerUser.retailerId,null,null)
            series.add(new SocialActivityHistory("views", views))
            val likes = retailerViewDao.getFacebookLikesHistory(partnerUser.retailerId,null,null)
            series.add(new SocialActivityHistory("likes", likes))
            val amount = retailerViewDao.getSalesAmountHistory(partnerUser.retailerId, null, null)
            series.add(new SocialActivityHistory("Sales($)",amount))
            val purchases = retailerViewDao.getSalesVolumeHistory(partnerUser.retailerId,null,null)
            series.add(new SocialActivityHistory("Sales(#)", purchases))
            self.channel ! GetRetailerSocialActivityByDateResponse(msg, Right(new RetailerSocialActivityByDate(partnerUser.retailerId,series)))

        case msg: GetProductSocialSummary =>
            logger.debug("Getting Product Social Summary {} {}", msg.productId, partnerUser.retailerId)
            val resultSet = retailerViewDao.getSocialActivityByProductIdAndRetailerId(msg.productId, partnerUser.retailerId)
            logger.debug("Result Set: {}", resultSet)
            self.channel ! GetProductSocialSummaryResponse(msg, Right(resultSet))

        case msg: GetProductSocialActivityByDate =>
            var series = new ArrayList[SocialActivityHistory]
            logger.debug("Getting Product Social Actvity By Date {}", msg productId)
            val comments = retailerViewDao.getFacebookCommentsHistory(partnerUser.retailerId,null, msg.productId)
            series.add(new SocialActivityHistory("comments",comments))
            val likes = retailerViewDao.getFacebookLikesHistory(partnerUser.retailerId,null, msg.productId)
            series.add(new SocialActivityHistory("likes",likes))
            val views = retailerViewDao.getEchoClicksHistory(partnerUser.retailerId,null, msg.productId)
            series.add(new SocialActivityHistory("views",views))
            val purchases = retailerViewDao.getSalesVolumeHistory(partnerUser.retailerId,null,msg.productId)
            series.add(new SocialActivityHistory("Sales(#)",purchases))
            self.channel ! GetProductSocialActivityByDateResponse(msg, Right(new RetailerProductSocialActivityByDate(partnerUser.retailerId,msg.productId,series)))


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
