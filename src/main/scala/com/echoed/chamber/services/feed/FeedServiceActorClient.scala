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

}
