package com.echoed.chamber.services.partneruser

import com.echoed.chamber.domain._
import com.echoed.chamber.domain.views.{CustomerSocialSummary,SocialActivityHistory, PartnerSocialSummary}
import com.echoed.chamber.dao.partner.PartnerUserDao
import com.echoed.chamber.dao.views.PartnerViewDao
import partner.PartnerUser
import scala.collection.JavaConversions._
import java.util.ArrayList
import views.{PartnerProductSocialActivityByDate,PartnerSocialActivityByDate,PartnerCustomerSocialActivityByDate}

import scalaz._
import Scalaz._
import akka.actor.PoisonPill
import com.echoed.chamber.services.EchoedActor


class PartnerUserServiceActor(
        var partnerUser: PartnerUser,
        partnerUserDao: PartnerUserDao,
        partnerViewDao: PartnerViewDao) extends EchoedActor {

    def handle = {
        case msg @ ActivatePartnerUser(password) =>
            val channel = context.sender

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
            val channel = context.sender

            try {
                assert(partnerUser.id == partnerUserId)
                channel ! LogoutResponse(msg, Right(true))
                self ! PoisonPill
                log.debug("Logged out {}", partnerUser)
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(PartnerUserException("Could not logout", e)))
                    log.error("Unexpected error processing %s" format msg, e)
            }

        case msg: GetPartnerUser =>
            sender ! GetPartnerUserResponse(msg, Right(partnerUser))

        case msg: GetPartnerSettings =>
            Option(partnerViewDao.getPartnerSettings(partnerUser.partnerId)).cata(
                resultSet => sender ! GetPartnerSettingsResponse(msg, Right(asScalaBuffer(resultSet).toList)),
                sender ! GetPartnerSettingsResponse(msg, Left(PartnerUserException("Partner Settings not available"))))

        case msg: GetCustomerSocialSummary =>
            val echoedUser = partnerViewDao.getEchoedUserByPartnerUser(msg.echoedUserId, partnerUser.partnerId)
            if (echoedUser == null || echoedUser.id == null) {
                sender ! GetCustomerSocialSummaryResponse(msg, Left(PartnerUserException("Error retrieving Echoed user")))
            } else {
                val likes = partnerViewDao.getTotalFacebookLikes(partnerUser.partnerId, msg.echoedUserId, null)
                val comments  = partnerViewDao.getTotalFacebookComments(partnerUser.partnerId,msg.echoedUserId, null)
                val clicks = partnerViewDao.getTotalEchoClicks(partnerUser.partnerId,msg.echoedUserId, null)
                val echoes = partnerViewDao.getTotalEchoes(partnerUser.partnerId,msg.echoedUserId,null)
                val friends = partnerViewDao.getTotalFacebookFriendsByEchoedUser(msg.echoedUserId)
                val volume = partnerViewDao.getTotalSalesVolume(partnerUser.partnerId,msg.echoedUserId, null)
                val sales = partnerViewDao.getTotalSalesAmount(partnerUser.partnerId, msg.echoedUserId, null)
                sender ! GetCustomerSocialSummaryResponse(msg, Right(new CustomerSocialSummary(echoedUser.id,echoedUser.name,echoes,likes,comments,clicks,friends,sales,volume)))
            }

        case msg: GetCustomerSocialActivityByDate =>
            var series = new ArrayList[SocialActivityHistory]

            val likes = partnerViewDao.getFacebookLikesHistory(partnerUser.partnerId,msg.echoedUserId, null)
            series.add(new SocialActivityHistory("likes",likes))
            val comments = partnerViewDao.getFacebookLikesHistory(partnerUser.partnerId,msg.echoedUserId,null)
            series.add(new SocialActivityHistory("comments",comments))
            val views = partnerViewDao.getEchoClicksHistory(partnerUser.partnerId,msg.echoedUserId,null)
            series.add(new SocialActivityHistory("views",views))
            val purchases = partnerViewDao.getSalesVolumeHistory(partnerUser.partnerId,msg.echoedUserId, null)
            series.add(new SocialActivityHistory("Sales(#)",purchases))

            sender ! GetCustomerSocialActivityByDateResponse(msg, Right(new PartnerCustomerSocialActivityByDate(partnerUser.partnerId,msg.echoedUserId,series)))

        case msg: GetPartnerSocialSummary =>
            val likes = partnerViewDao.getTotalFacebookLikes(partnerUser.partnerId, null, null)
            val comments  = partnerViewDao.getTotalFacebookComments(partnerUser.partnerId, null, null)
            val clicks = partnerViewDao.getTotalEchoClicks(partnerUser.partnerId, null, null)
            val echoes = partnerViewDao.getTotalEchoes(partnerUser.partnerId, null,null)
            val volume = partnerViewDao.getTotalSalesVolume(partnerUser.partnerId, null, null)
            val sales = partnerViewDao.getTotalSalesAmount(partnerUser.partnerId, null, null)
            sender ! GetPartnerSocialSummaryResponse(
                    msg,
                    Right(new PartnerSocialSummary(partnerUser.partnerId, partnerUser.name, echoes, likes, comments, clicks, sales, volume)))

        case msg: GetPartnerSocialActivityByDate =>
            var series = new ArrayList[SocialActivityHistory]

            val comments = partnerViewDao.getFacebookCommentsHistory(partnerUser.partnerId,null,null)
            series.add(new SocialActivityHistory("comments",comments))
            val views = partnerViewDao.getEchoClicksHistory(partnerUser.partnerId,null,null)
            series.add(new SocialActivityHistory("views", views))
            val likes = partnerViewDao.getFacebookLikesHistory(partnerUser.partnerId,null,null)
            series.add(new SocialActivityHistory("likes", likes))
            val amount = partnerViewDao.getSalesAmountHistory(partnerUser.partnerId, null, null)
            series.add(new SocialActivityHistory("Sales($)",amount))
            val purchases = partnerViewDao.getSalesVolumeHistory(partnerUser.partnerId,null,null)
            series.add(new SocialActivityHistory("Sales(#)", purchases))
            sender ! GetPartnerSocialActivityByDateResponse(msg, Right(new PartnerSocialActivityByDate(partnerUser.partnerId,series)))

        case msg: GetProductSocialSummary =>
            Option(partnerViewDao.getSocialActivityByProductIdAndPartnerId(msg.productId, partnerUser.partnerId)).cata(
                resultSet => sender ! GetProductSocialSummaryResponse(msg, Right(resultSet)),
                sender ! GetProductSocialSummaryResponse(msg, Left(PartnerUserException("Product summary not available"))))

        case msg: GetProductSocialActivityByDate =>
            var series = new ArrayList[SocialActivityHistory]
            log.debug("Getting Product Social Actvity By Date {}", msg productId)
            val comments = partnerViewDao.getFacebookCommentsHistory(partnerUser.partnerId,null, msg.productId)
            series.add(new SocialActivityHistory("comments",comments))
            val likes = partnerViewDao.getFacebookLikesHistory(partnerUser.partnerId,null, msg.productId)
            series.add(new SocialActivityHistory("likes",likes))
            val views = partnerViewDao.getEchoClicksHistory(partnerUser.partnerId,null, msg.productId)
            series.add(new SocialActivityHistory("views",views))
            val purchases = partnerViewDao.getSalesVolumeHistory(partnerUser.partnerId,null,msg.productId)
            series.add(new SocialActivityHistory("Sales(#)",purchases))
            sender ! GetProductSocialActivityByDateResponse(msg, Right(new PartnerProductSocialActivityByDate(partnerUser.partnerId,msg.productId,series)))


        case msg: GetProducts =>
            Option(partnerViewDao.getProductsWithPartnerId(partnerUser.partnerId, 0, 25)).cata(
                resultSet => sender ! GetProductsResponse(msg, Right(resultSet)),
                sender ! GetProductsResponse(msg, Left(PartnerUserException("Products not available"))))

        case msg: GetTopProducts =>
            Option(partnerViewDao.getTopProductsWithPartnerId(partnerUser.partnerId)).cata(
                resultSet => sender ! GetTopProductsResponse(msg, Right(resultSet)),
                sender ! GetTopProductsResponse(msg, Left(PartnerUserException("Top products not available"))))

        case msg: GetCustomers =>
            Option(partnerViewDao.getCustomersWithPartnerId(partnerUser.partnerId,0,25)).cata(
                resultSet => sender ! GetCustomersResponse(msg, Right(resultSet)),
                sender ! GetCustomersResponse(msg, Left(PartnerUserException("Customers not available"))))

        case msg: GetTopCustomers =>
            Option(partnerViewDao.getTopCustomersWithPartnerId(partnerUser.partnerId)).cata(
                resultSet => sender ! GetTopCustomersResponse(msg, Right(resultSet)),
                sender ! GetTopCustomersResponse(msg, Left(PartnerUserException("Top customers not available"))))

        case msg: GetEchoes =>
            Option(partnerViewDao.getPartnerEchoView(partnerUser.partnerId)).cata(
                resultSet => sender ! GetEchoesResponse(msg, Right(asScalaBuffer(resultSet).toList)),
                sender ! GetEchoesResponse(msg, Left(PartnerUserException("Echoes Not Available"))))

        case msg: GetComments =>
            Option(partnerViewDao.getComments(partnerUser.partnerId, null, null)).cata(
                resultSet => sender ! GetCommentsResponse(msg, Right(asScalaBuffer(resultSet).toList)),
                sender ! GetCommentsResponse(msg, Left(PartnerUserException("Comments not available"))))

        case msg: GetCommentsByProductId =>
            Option(partnerViewDao.getComments(partnerUser.partnerId, null, msg.productId)).cata(
                resultSet => sender ! GetCommentsByProductIdResponse(msg, Right(asScalaBuffer(resultSet).toList)),
                sender ! GetCommentsByProductIdResponse(msg, Left(PartnerUserException("Comments by product not available"))))

        case msg: GetEchoClickGeoLocation =>
            Option(partnerViewDao.getEchoClickGeoLocation(partnerUser.partnerId, null, null)).cata(
                resultSet => sender ! GetEchoClickGeoLocationResponse(msg, Right(asScalaBuffer(resultSet).toList)),
                sender ! GetEchoClickGeoLocationResponse(msg, Left(PartnerUserException("Geolocation For Echo Clicks Not Available")))
            )
    }

}
