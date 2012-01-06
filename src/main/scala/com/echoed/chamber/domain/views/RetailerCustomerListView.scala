package com.echoed.chamber.domain.views
import java.util.{ArrayList, List => JList}

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 1/6/12
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */

case class RetailerCustomerListView(
    retailerId: String,
    retailerName: String,
    customers: JList[CustomerSocialSummary]){

    def this(retailerId: String, retailerName: String) = this(retailerId, retailerName, new ArrayList[CustomerSocialSummary])
}