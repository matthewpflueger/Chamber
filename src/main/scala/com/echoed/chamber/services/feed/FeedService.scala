package com.echoed.chamber.services.feed

import akka.dispatch.Future
/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 4/16/12
 * Time: 9:23 AM
 * To change this template use File | Settings | File Templates.
 */

trait FeedService {

    //val id: String;

    def getPublicFeed: Future[GetPublicFeedResponse]

}
