package com.echoed.chamber.services.tag


import scala.collection.JavaConversions._
import com.echoed.chamber.services.{EventProcessorActorSystem, EventProcessor, EchoedActor}
import com.echoed.chamber.dao.TagDao
import com.echoed.chamber.domain.Tag
import collection.immutable.TreeMap
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._

class TagServiceActor(
        eventProcessor: EventProcessorActorSystem,
        tagDao: TagDao) extends EchoedActor {

    var tagMap = Map[String, Tag]()
    var treeMap = new TreeMap[String, Tag]

    eventProcessor.subscribe(self, classOf[TagAdded])
    eventProcessor.subscribe(self, classOf[TagReplaced])

    private implicit val timeout = Timeout(20 seconds)

    override def preStart(){
        val tags = asScalaBuffer(tagDao.getTags).toList
        tags.map(tag => tagMap += (tag.id.toLowerCase -> tag))
        tags.map(tag => treeMap += (tag.id.toLowerCase -> tag))
    }

    def handle = {

        //EVENTS
        case msg @ TagAdded(tagId) =>
            log.debug("Received Event Tag Added {}", tagId)
            self ! AddTag(tagId)

        case msg @ TagReplaced(originalTagId, newTagId) =>
            self ! ReplaceTag(originalTagId, newTagId)

        //COMMANDS
        case msg @ GetTags(filter) =>
            val channel = context.sender
            val tags = treeMap.values.filter( t => { t.id.toLowerCase.startsWith(filter.toLowerCase) && t.counter > 0  }).toList
            channel ! GetTagsResponse(msg, Right(tags))

        case msg @ ApproveTag(tagId) =>
            val channel = context.sender
            try {
                var tag = treeMap.get(tagId.toLowerCase).get
                tag = tag.copy(approved = true)
                treeMap += (tagId -> tag)
                self ! WriteTag(tagId)
                channel ! ApproveTagResponse(msg, Right(tag))
            } catch {
                case e =>
                    log.error("Unexpected error processing: {}", e)
            }

        case msg @ ReplaceTag(originalTagId, newTagId) =>
            val channel = context.sender
            (self ? AddTag(newTagId)).onSuccess{
                case AddTagResponse(_, Right(tag)) =>
                    if(originalTagId != null) {
                        self ! RemoveTag(originalTagId)
                    }
                    channel ! ReplaceTagResponse(msg, Right(tag))
            }

        case msg @ AddTag(tagId) =>
            val channel = context.sender
            var tag = treeMap.get(tagId.toLowerCase).getOrElse(new Tag(tagId, 0, false))
            tag = tag.copy(counter = tag.counter + 1)
            treeMap += (tagId.toLowerCase -> tag)
            self ! WriteTag(tagId)
            channel ! AddTagResponse(msg, Right(tag))

        case msg @ RemoveTag(tagId) =>
            val channel = context.sender
            var tag = treeMap.get(tagId.toLowerCase).getOrElse(new Tag(tagId, 0 , false))
            tag = tag.copy(counter = { if(tag.counter > 0) tag.counter - 1 else 0})
            treeMap += (tagId.toLowerCase -> tag)
            self ! WriteTag(tagId)
            channel ! RemoveTagResponse(msg, Right(tag))

        case msg @ WriteTag(tagId) =>
            val channel = context.sender
            treeMap.get(tagId.toLowerCase).map{
                tag =>
                    tagDao.insert(tag)
                    channel ! WriteTagResponse(msg, Right(tag))
            }
    }

}
