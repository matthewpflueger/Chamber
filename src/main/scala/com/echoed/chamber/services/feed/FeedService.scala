package com.echoed.chamber.services.feed

import akka.dispatch.Future


trait FeedService {

    def getPublicFeed: Future[GetPublicFeedResponse]

    def getPublicFeed(page: Int): Future[GetPublicFeedResponse]

    def getPublicStoryFeed(page: Int): Future[GetPublicStoryFeedResponse]

    def getPublicCategoryFeed(categoryId: String, page: Int): Future[GetPublicCategoryFeedResponse]

    def getUserPublicFeed(echoedUserId: String, page: Int): Future[GetUserPublicFeedResponse]

    def getPartnerFeed(partnerId: String, page: Int): Future[GetPartnerFeedResponse]

    def getPartnerStoryFeed(partnerId: String, page: Int): Future[GetPartnerStoryFeedResponse]

    def getStory(storyId: String): Future[GetStoryResponse]

    def getTags(partialTagId: String): Future[GetTagsResponse]

    def getStoryIds: Future[GetStoryIdsResponse]

    def getPartnerIds: Future[GetPartnerIdsResponse]
}
