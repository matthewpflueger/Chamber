package com.echoed.chamber.dao.views

import com.echoed.chamber.domain.{FacebookComment}
import com.echoed.chamber.domain.EchoedUser
import org.apache.ibatis.annotations.Param
import java.util.{List => JList}
import com.echoed.chamber.domain.views._


trait PartnerViewDao {
    
    def getEchoedUserByPartnerUser(
            @Param("echoedUserId") echoedUserId: String,
            @Param("partnerId") partnerId: String): EchoedUser

    def getTotalFacebookFriendsByEchoedUser(@Param("echoedUserId") echoedUserId: String): Int


    def getSocialActivityByPartnerId(partnerId: String): PartnerSocialSummary

    def getSocialActivityByProductIdAndPartnerId(
         @Param("productId") productId: String,
         @Param("partnerId") partnerId: String): ProductSocialSummary
    
    def getProductsWithPartnerId(
            @Param("partnerId") partnerId: String,
            @Param("start") start: Int, 
            @Param("limit") limit: Int): PartnerProductsListView

    def getTopProductsWithPartnerId(partnerId: String): PartnerProductsListView
    
    def getCustomersWithPartnerId(
            @Param("partnerId") partnerId: String,
            @Param("start") start: Int,
            @Param("limit") limit: Int): PartnerCustomerListView

    def getTopCustomersWithPartnerId(partnerId: String): PartnerCustomerListView

    def getFacebookLikesHistory(
            @Param("partnerId") partnerId: String,
            @Param("echoedUserId") echoedUserId: String,
            @Param("productId") productId: String): JList[SocialActivityTotalByDate]
    
    def getFacebookCommentsHistory(
            @Param("partnerId") partnerId: String,
            @Param("echoedUserId") echoedUserId: String,
            @Param("productId") productId: String): JList[SocialActivityTotalByDate]

    def getEchoClicksHistory(
            @Param("partnerId") partnerId: String,
            @Param("echoedUserId") echoedUserId: String,
            @Param("productId") productId: String): JList[SocialActivityTotalByDate]

    def getSalesAmountHistory(
            @Param("partnerId") partnerId: String,
            @Param("echoedUserId") echoedUserId: String,
            @Param("productId") productId:String): JList[SocialActivityTotalByDate]

    def getSalesVolumeHistory(
            @Param("partnerId") partnerId: String,
            @Param("echoedUserId") echoedUserId: String, 
            @Param("productId") productId:String): JList[SocialActivityTotalByDate]
    
    def getComments(
            @Param("partnerId") partnerId: String,
            @Param("echoedUserId") echoedUserId: String,
            @Param("productId") productId: String): JList[FacebookComment]

    def getTotalSalesAmount(
            @Param("partnerId") partnerId: String,
            @Param("echoedUserId") echoedUserId: String, 
            @Param("productId") productId: String) : Float
    
    def getTotalSalesVolume(
            @Param("partnerId") partnerId: String,
            @Param("echoedUserId") echoedUserId: String,
            @Param("productId") productId: String): Int
    
    def getTotalFacebookComments(
            @Param("partnerId") partnerId: String,
            @Param("echoedUserId") echoedUserId: String, 
            @Param("productId") productId: String): Int
    
    def getTotalEchoClicks(
            @Param("partnerId") partnerId: String,
            @Param("echoedUserId") echoedUserId: String, 
            @Param("productId") productId: String) : Int
    
    def getTotalFacebookLikes(
            @Param("partnerId") partnerId: String,
            @Param("echoedUserId") echoedUserId: String, 
            @Param("productId") productId: String) : Int

    def getTotalEchoes(
            @Param("partnerId") partnerId: String,
            @Param("echoedUserId") echoedUserId: String, 
            @Param("productId") productId: String): Int
}