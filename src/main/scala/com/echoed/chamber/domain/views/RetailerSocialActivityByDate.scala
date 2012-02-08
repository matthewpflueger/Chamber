package com.echoed.chamber.domain.views
import java.util.{ArrayList, List => JList}

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 1/24/12
 * Time: 10:17 AM
 * To change this template use File | Settings | File Templates.
 */

case class RetailerSocialActivityByDate(
       retailerId: String,
       series: JList[SocialActivityHistory]){


}