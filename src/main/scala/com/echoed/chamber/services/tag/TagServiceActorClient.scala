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

    def getTopTags = (tagServiceActor ? GetTopTags()).mapTo[GetTopTagsResponse]

    def addTag(tagId: String) = (tagServiceActor ? AddTag(tagId.toLowerCase)).mapTo[AddTagResponse]

    def removeTag(tagId: String) = (tagServiceActor ? RemoveTag(tagId.toLowerCase)).mapTo[RemoveTagResponse]

    def replaceTag(ogTagId: String, newTagId: String) = (tagServiceActor ? ReplaceTag(ogTagId.toLowerCase, newTagId.toLowerCase)).mapTo[ReplaceTagResponse]

}
