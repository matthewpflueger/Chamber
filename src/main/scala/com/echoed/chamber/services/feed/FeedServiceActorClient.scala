package com.echoed.chamber.services.feed

import akka.actor.ActorRef
import com.echoed.chamber.domain.views.{Closet,Feed}
import com.echoed.chamber.services.ActorClient
import reflect.BeanProperty

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 4/16/12
 * Time: 9:23 AM
 * To change this template use File | Settings | File Templates.
 */

class FeedServiceActorClient extends FeedService with ActorClient {

    @BeanProperty var feedServiceActor: ActorRef = _

    //val id = feedServiceActor.id

    def actorRef = feedServiceActor

    def getPublicFeed = (feedServiceActor ? GetPublicFeed(0)).mapTo[GetPublicFeedResponse]

}
