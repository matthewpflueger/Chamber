package com.echoed.chamber.services.tag

import com.echoed.chamber.services.ActorClient
import akka.actor.ActorRef
import reflect.BeanProperty
import akka.util.Timeout
import akka.util.duration._
import akka.pattern.ask

class TagServiceActorClient extends TagService with ActorClient with Serializable {

    @BeanProperty var tagServiceActor: ActorRef = _

    private implicit val timeout = Timeout(20 seconds)

    def actorRef = tagServiceActor

    def getTags(filter: String) = (tagServiceActor ? GetTags(filter)).mapTo[GetTagsResponse]

    def addTag(tagId: String) = (tagServiceActor ? AddTag(tagId)).mapTo[AddTagResponse]

}
