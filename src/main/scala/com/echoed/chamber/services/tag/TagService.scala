package com.echoed.chamber.services.tag


import scala.collection.JavaConversions._
import com.echoed.chamber.services.{EventProcessorActorSystem, EventProcessor, EchoedService}
import com.echoed.chamber.dao.TagDao
import com.echoed.chamber.domain.Tag
import collection.immutable.TreeMap
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import com.echoed.chamber.domain.views.CommunityFeed

class TagService(
        eventProcessor: EventProcessorActorSystem,
        tagDao: TagDao) extends EchoedService {


    implicit object PopularityOrdering extends Ordering[(Int, String)] {
        def compare(a:(Int, String), b:(Int, String)) = {
            if( (b._1 compare a._1) != 0)
                b._1 compare a._1
            else
                a._2 compare b._2
        }
    }


    var tagMap = Map[String, Tag]()
    var treeMap = new TreeMap[String, Tag]
    var popularMap = new TreeMap[(Int, String), Tag]()(PopularityOrdering)

    eventProcessor.subscribe(self, classOf[TagAdded])
    eventProcessor.subscribe(self, classOf[TagReplaced])

    private implicit val timeout = Timeout(20 seconds)

    override def preStart(){
        val tags = asScalaBuffer(tagDao.getTags).toList
        tags.map(addToCaches(_))
    }

    def addToCaches(tag: Tag){
        tagMap += (tag.id.toLowerCase -> tag)
        treeMap += (tag.id.toLowerCase -> tag)
        popularMap += ((tag.counter, tag.id.toLowerCase) -> tag)
    }

    def removeFromCaches(tag: Tag){
        tagMap -= (tag.id.toLowerCase)
        treeMap -= (tag.id.toLowerCase)
        popularMap -= ((tag.counter, tag.id.toLowerCase))
    }

    def handle = {

        //EVENTS
        case msg @ TagAdded(tagId) =>
            log.debug("Received Event Tag Added {}", tagId)
            self ! AddTag(tagId)

        case msg @ TagReplaced(originalTagId, newTagId) =>
            self ! ReplaceTag(Option(originalTagId), newTagId)

        //COMMANDS
        case msg @ GetTags(filter) =>
            val channel = context.sender
            val tags = treeMap.values.filter( t => { t.id.toLowerCase.startsWith(filter.toLowerCase) && t.counter > 0 }).toList
            channel ! GetTagsResponse(msg, Right(new CommunityFeed(tags)))

        case msg: GetTopTags =>
            val channel = context.sender
            val tags = popularMap.values.filter(_.counter > 0).slice(0, 10).toList
            log.debug("Tags: {}", tags)
            channel ! GetTopTagsResponse(msg, Right(new CommunityFeed(tags)))

        case msg @ ApproveTag(tagId) =>
            val channel = context.sender
            try {
                var tag = treeMap.get(tagId.toLowerCase).get
                tag = tag.copy(approved = true)
                treeMap += (tagId -> tag)
                popularMap += ((tag.counter, tag.id) -> tag)
                self ! WriteTag(tagId)
                channel ! ApproveTagResponse(msg, Right(tag))
            } catch {
                case e =>
                    log.error("Unexpected error processing: {}", e)
            }

        case msg @ ReplaceTag(originalTagId, newTagId) =>
            val channel = context.sender
            originalTagId.foreach(self ! RemoveTag(_))
            (self ? AddTag(newTagId)).onSuccess{
                case AddTagResponse(_, Right(tag)) =>
                    channel ! ReplaceTagResponse(msg, Right(tag))
            }

        case msg @ AddTag(tagId) =>
            val channel = context.sender
            if(tagId != ""){
                var tag = treeMap.get(tagId.toLowerCase).getOrElse(new Tag(tagId, 0, false))
                removeFromCaches(tag)
                tag = tag.copy(counter = tag.counter + 1)
                addToCaches(tag)
                self ! WriteTag(tagId)
                channel ! AddTagResponse(msg, Right(tag))
            }

        case msg @ RemoveTag(tagId) =>
            val channel = context.sender
            treeMap.get(tagId.toLowerCase).map({
                case t: Tag =>
                    var tag = t
                    removeFromCaches(tag)
                    tag = tag.copy(counter = { if(tag.counter > 0) tag.counter - 1 else 0})
                    addToCaches(tag)
                    self ! WriteTag(tagId)
                    channel ! RemoveTagResponse(msg, Right(tag))
                case t =>
                    channel ! RemoveTagResponse(msg, Left(TagException("No Tag To Remove")))
            })




        case msg @ WriteTag(tagId) =>
            val channel = context.sender
            treeMap.get(tagId.toLowerCase).map{
                tag =>
                    tagDao.insert(tag)
                    channel ! WriteTagResponse(msg, Right(tag))
            }
    }

}
