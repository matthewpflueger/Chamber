package com.echoed.chamber.services.tag


import scala.collection.JavaConversions._
import com.echoed.chamber.services.EchoedActor
import com.echoed.chamber.dao.TagDao
import com.echoed.chamber.domain.Tag
import collection.immutable.TreeMap

class TagServiceActor(
        tagDao: TagDao) extends EchoedActor {

    var tagMap = Map[String, Tag]()
    var treeMap = new TreeMap[String, Tag]

    override def preStart(){
        val tags = asScalaBuffer(tagDao.getTags).toList
        log.debug("Tags: {}", tags)
        tags.map(tag => tagMap += (tag.id.toLowerCase -> tag))
        tags.map(tag => treeMap += (tag.id.toLowerCase -> tag))
    }

    def handle = {

        case msg @ GetTags(filter) =>
            val channel = context.sender
            val tags = treeMap.values.filter(_.id.toLowerCase.startsWith(filter.toLowerCase)).toList
            channel ! GetTagsResponse(msg, Right(tags))

        case msg @ AddTag(tagId) =>
            val channel = context.sender
            val lowerTagId = tagId.toLowerCase
            var tag = treeMap.get(lowerTagId).getOrElse(new Tag(tagId, 0, false))
            tag = tag.copy(counter = tag.counter + 1)
            treeMap += (tag.id.toLowerCase -> tag)
            self ! WriteTag(lowerTagId)
            channel ! AddTagResponse(msg, Right(tag))

        case msg @ DecreaseTagCount(tagId) =>
            val channel = context.sender

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
