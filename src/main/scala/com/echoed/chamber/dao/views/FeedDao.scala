package com.echoed.chamber.dao.views

import com.echoed.chamber.domain.views.Feed

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 12/13/11
 * Time: 9:14 PM
 * To change this template use File | Settings | File Templates.
 */

trait FeedDao {

    def findByEchoedUserId(echoedUserId: String): Feed

}