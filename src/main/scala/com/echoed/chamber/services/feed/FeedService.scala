package com.echoed.chamber.services.feed

import akka.dispatch.Future


trait FeedService {

    def getPublicFeed: Future[GetPublicFeedResponse]

    def getPublicFeed(page: Int): Future[GetPublicFeedResponse]

    def getUserPublicFeed(echoedUserId: String, page: Int): Future[GetUserPublicFeedResponse]

    def getPartnerFeed(partnerId: String, page: Int): Future[GetPartnerFeedResponse]

}
