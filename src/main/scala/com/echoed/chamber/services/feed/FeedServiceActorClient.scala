package com.echoed.chamber.services.feed

import akka.actor.ActorRef
import com.echoed.chamber.domain.views.{Closet,Feed}
import com.echoed.chamber.services.ActorClient
import reflect.BeanProperty
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._


class FeedServiceActorClient extends FeedService with ActorClient with Serializable {

    @BeanProperty var feedServiceActor: ActorRef = _

    private implicit val timeout = Timeout(20 seconds)

    def actorRef = feedServiceActor

    def getPublicFeed = (feedServiceActor ? GetPublicFeed(0)).mapTo[GetPublicFeedResponse]

    def getPublicFeed(page: Int) = (feedServiceActor ? GetPublicFeed(page)).mapTo[GetPublicFeedResponse]

    def getPublicStoryFeed(page: Int) = (feedServiceActor ? GetPublicStoryFeed(page)).mapTo[GetPublicStoryFeedResponse]

    def getPublicCategoryFeed(categoryId: String, page: Int) = (feedServiceActor ? GetPublicCategoryFeed(categoryId, page)).mapTo[GetPublicCategoryFeedResponse]

    def getUserPublicFeed(echoedUserId: String, page: Int) = (feedServiceActor ? GetUserPublicFeed(echoedUserId, page)).mapTo[GetUserPublicFeedResponse]

    def getPartnerFeed(partnerId: String, page: Int) = (feedServiceActor ? GetPartnerFeed(partnerId, page)).mapTo[GetPartnerFeedResponse]

    def getPartnerStoryFeed(partnerId: String, page: Int) = (feedServiceActor ? GetPartnerStoryFeed(partnerId, page)).mapTo[GetPartnerStoryFeedResponse]

    def getStory(storyId: String) = (feedServiceActor ? GetStory(storyId)).mapTo[GetStoryResponse]

    def getTags(partialTagId: String) = (feedServiceActor ? GetTags(partialTagId)).mapTo[GetTagsResponse]

    def getStoryIds = (feedServiceActor ? GetStoryIds()).mapTo[GetStoryIdsResponse]

    def getPartnerIds = (feedServiceActor ? GetPartnerIds()).mapTo[GetPartnerIdsResponse]
}
