package com.echoed.chamber.services.feed

import akka.dispatch.Future


trait FeedService {

    def getPublicFeed: Future[GetPublicFeedResponse]

}
