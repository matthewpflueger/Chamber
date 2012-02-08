package com.echoed.chamber.domain.views
import java.util.{ArrayList, List => JList}

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 1/25/12
 * Time: 2:44 PM
 * To change this template use File | Settings | File Templates.
 */

case class RetailerCustomerSocialActivityByDate(
                                                   retailerId: String,
                                                   echoedUserId: String,
                                                   series: JList[SocialActivityHistory]
                                                   ) {

}