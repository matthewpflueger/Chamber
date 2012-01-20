package com.echoed.chamber.dao.views

import com.echoed.chamber.domain.views.{RetailerSocialSummary, ProductSocialSummary, RetailerProductsListView, RetailerCustomerListView}
import org.apache.ibatis.annotations.Param

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

}