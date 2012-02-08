package com.echoed.chamber.domain.views
import java.util.{ArrayList, List => JList}

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/2/12
 * Time: 7:41 PM
 * To change this template use File | Settings | File Templates.
 */

case class SocialActivityHistory(
    name: String,
    data: JList[SocialActivityTotalByDate]
)

