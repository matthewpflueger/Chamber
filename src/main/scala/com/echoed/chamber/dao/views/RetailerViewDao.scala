package com.echoed.chamber.dao.views

import com.echoed.chamber.domain.views.{RetailerSocialSummary, ProductSocialSummary, RetailerProductsListView, RetailerCustomerListView, SocialActivityTotalByDate,ProductSocialActivityTotal}
import com.echoed.chamber.domain.{FacebookComment}
import com.echoed.chamber.domain.EchoedUser
import org.apache.ibatis.annotations.Param
import java.util.{List => JList}

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 1/5/12
 * Time: 9:21 AM
 * To change this template use File | Settings | File Templates.
 */

trait RetailerViewDao {
    
    def getEchoedUserByRetailerUser(
            @Param("echoedUserId") echoedUserId: String,
            @Param("retailerId") retailerId: String): EchoedUser



    def getTotalFacebookFriendsByEchoedUser(@Param("echoedUserId") echoedUserId: String): Int

    def getTotalFacebookLikesByRetailerIdProductId(
            @Param("productId") productId: String,
            @Param("retailerId") retailerId: String): ProductSocialActivityTotal

    def getTotalFacebookCommentsByRetailerIdProductId(
            @Param("productId") productId: String,
            @Param("retailerId") retailerId: String): ProductSocialActivityTotal

    def getTotalEchoClicksByRetailerIdProductId(
            @Param("productId") productId: String,
            @Param("retailerId") retailerId: String): ProductSocialActivityTotal

    def getSocialActivityByRetailerId(retailerId: String): RetailerSocialSummary

    def getSocialActivityByProductIdAndRetailerId(
         @Param("productId") productId: String,
         @Param("retailerId") retailerId: String): ProductSocialSummary
    
    def getProductsWithRetailerId(
            @Param("retailerId") retailerId: String,
            @Param("start") start: Int, 
            @Param("limit") limit: Int): RetailerProductsListView

    def getTopProductsWithRetailerId(retailerId: String): RetailerProductsListView
    
    def getCustomersWithRetailerId(
            @Param("retailerId") retailerId: String,
            @Param("start") start: Int,
            @Param("limit") limit: Int): RetailerCustomerListView

    def getTopCustomersWithRetailerId(retailerId: String): RetailerCustomerListView

    def getFacebookLikesHistory(
            @Param("retailerId") retailerId: String,
            @Param("echoedUserId") echoedUserId: String,
            @Param("productId") productId: String): JList[SocialActivityTotalByDate]
    
    def getFacebookCommentsHistory(
            @Param("retailerId") retailerId: String,
            @Param("echoedUserId") echoedUserId: String,
            @Param("productId") productId: String): JList[SocialActivityTotalByDate]

    def getEchoClicksHistory(
            @Param("retailerId") retailerId: String,
            @Param("echoedUserId") echoedUserId: String,
            @Param("productId") productId: String): JList[SocialActivityTotalByDate]

    def getSalesAmountHistory(
            @Param("retailerId") retailerId: String,
            @Param("echoedUserId") echoedUserId: String,
            @Param("productId") productId:String): JList[SocialActivityTotalByDate]

    def getSalesVolumeHistory(
            @Param("retailerId") retailerId: String, 
            @Param("echoedUserId") echoedUserId: String, 
            @Param("productId") productId:String): JList[SocialActivityTotalByDate]

    def getCommentsByRetailerIdProductId(
            @Param("retailerId") retailerId: String,
            @Param("productId") productId: String): JList[FacebookComment]

    def getCommentsByRetailerId(
            @Param("retailerId") retailerId: String): JList[FacebookComment]
    
    def getTotalSalesAmount(
            @Param("retailerId") retailerId: String, 
            @Param("echoedUserId") echoedUserId: String, 
            @Param("productId") productId: String) : Float
    
    def getTotalSalesVolume(
            @Param("retailerId") retailerId: String,
            @Param("echoedUserId") echoedUserId: String,
            @Param("productId") productId: String): Int
    
    def getTotalFacebookComments(
            @Param("retailerId") retailerId: String, 
            @Param("echoedUserId") echoedUserId: String, 
            @Param("productId") productId: String): Int
    
    def getTotalEchoClicks(
            @Param("retailerId") retailerId: String, 
            @Param("echoedUserId") echoedUserId: String, 
            @Param("productId") productId: String) : Int
    
    def getTotalFacebookLikes(
            @Param("retailerId") retailerId: String, 
            @Param("echoedUserId") echoedUserId: String, 
            @Param("productId") productId: String) : Int
    
    def getTotalEchoes(
            @Param("retailerId") retailerId: String, 
            @Param("echoedUserId") echoedUserId: String, 
            @Param("productId") productId: String): Int
}