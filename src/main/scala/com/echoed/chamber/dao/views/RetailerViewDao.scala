package com.echoed.chamber.dao.views

import com.echoed.chamber.domain.views.{RetailerSocialSummary, ProductSocialSummary, RetailerProductsListView, RetailerCustomerListView, SocialActivityTotalByDate}
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

    def getSocialActivityByRetailerId(retailerId: String): RetailerSocialSummary

    def getSocialActivityByProductIdAndRetailerId(
         @Param("productId") productId: String,
         @Param("retailerId") retailerId: String): ProductSocialSummary

    def getTopProductsWithRetailerId(retailerId: String): RetailerProductsListView

    def getTopCustomersWithRetailerId(retailerId: String): RetailerCustomerListView
    
    def getFacebookLikesByRetailerIdProductIdDate(
            @Param("productId") productId: String,
            @Param("retailerId") retailerId: String): JList[SocialActivityTotalByDate]
    
    def getFacebookCommentsByRetailerIdProductIdDate(
            @Param("productId") productId: String,
            @Param("retailerId") retailerId: String): JList[SocialActivityTotalByDate]

    def getEchoClicksByRetailerIdProductIdDate(
            @Param("productId") productId: String,
            @Param("retailerId") retailerId: String): JList[SocialActivityTotalByDate]

}