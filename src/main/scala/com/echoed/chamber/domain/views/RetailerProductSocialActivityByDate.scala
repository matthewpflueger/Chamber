package com.echoed.chamber.domain.views
import java.util.{ArrayList, List => JList}

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 1/21/12
 * Time: 2:54 PM
 * To change this template use File | Settings | File Templates.
 */

case class RetailerProductSocialActivityByDate(
    retailerId: String,
    productId: String,
    likes: JList[SocialActivityTotalByDate],
    comments: JList[SocialActivityTotalByDate],
    echoClicks: JList[SocialActivityTotalByDate]){
}