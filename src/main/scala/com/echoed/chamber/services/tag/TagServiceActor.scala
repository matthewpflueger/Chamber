package com.echoed.chamber.services.tag


import scala.collection.JavaConversions._
import com.echoed.chamber.services.EchoedActor
import com.echoed.chamber.dao.TagDao
import com.echoed.chamber.domain.Tag
import collection.immutable.TreeMap
import akka.pattern.ask

class TagServiceActor(
        tagDao: TagDao) extends EchoedActor {

    var tagMap = Map[String, Tag]()
    var treeMap = new TreeMap[String, Tag]

    override def preStart(){
        val tags = asScalaBuffer(tagDao.getTags).toList
        tags.map(tag => tagMap += (tag.id.toLowerCase -> tag))
        tags.map(tag => treeMap += (tag.id.toLowerCase -> tag))
    }

    def handle = {

        case msg @ GetTags(filter) =>
            val channel = context.sender
            val tags = treeMap.values.filter(_.id.toLowerCase.startsWith(filter.toLowerCase)).toList
            channel ! GetTagsResponse(msg, Right(tags))

        case msg @ ApproveTag(tagId) =>
            val channel = context.sender
            val lowerTagId = tagId.toLowerCase
            try {
                val tag = treeMap.get(lowerTagId).get.copy(approved = true)
                treeMap += (lowerTagId -> tag)
                self ! WriteTag(lowerTagId)

            } catch {
                case e =>
                    log.error("Unexpected error processing: {}", e)
            }


        case msg @ ReplaceTag(originalTagId, newTagId) =>
            val channel = context.sender
            (self ? AddTag(newTagId)).onSuccess{
                case AddTagResponse(_, Right(tag)) =>
                    self ! RemoveTag(originalTagId)
                    channel ! ReplaceTagResponse(msg, Right(tag))
            }

        case msg @ AddTag(tagId) =>
            val channel = context.sender
            val lowerTagId = tagId.toLowerCase
            var tag = treeMap.get(lowerTagId).getOrElse(new Tag(tagId, 0, false))
            tag = tag.copy(counter = tag.counter + 1)
            treeMap += (lowerTagId -> tag)
            self ! WriteTag(lowerTagId)
            channel ! AddTagResponse(msg, Right(tag))

        case msg @ RemoveTag(tagId) =>
            val channel = context.sender
            val lowerTagId = tagId.toLowerCase

        case msg @ WriteTag(tagId) =>
            val channel = context.sender
            treeMap.get(tagId).map{
                tag =>
                    log.debug("Tag Found: {}", tag)
                    tagDao.insert(tag)
                    channel ! WriteTagResponse(msg, Right(tag))
            }
    }

}
