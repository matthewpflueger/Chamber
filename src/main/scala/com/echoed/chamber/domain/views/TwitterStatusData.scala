package com.echoed.chamber.domain.views
import com.echoed.chamber.domain.{TwitterUser,  TwitterStatus}

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 1/27/12
 * Time: 11:05 AM
 * To change this template use File | Settings | File Templates.
 */

case class
TwitterStatusData(
    id: String,
    twitterStatus: TwitterStatus,
    twitterUser: TwitterUser) {
    
    

}