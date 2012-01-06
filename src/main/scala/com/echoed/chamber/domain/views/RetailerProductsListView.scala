package com.echoed.chamber.domain.views
import java.util.{ArrayList, List => JList}
/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 1/5/12
 * Time: 4:44 PM
 * To change this template use File | Settings | File Templates.
 */

case class RetailerProductsListView(
    retailerId: String, 
    retailerName: String,
    products: JList[ProductSocialSummary]){

    def this(retailerId: String, retailerName: String) = this(retailerId, retailerName, new ArrayList[ProductSocialSummary])
}