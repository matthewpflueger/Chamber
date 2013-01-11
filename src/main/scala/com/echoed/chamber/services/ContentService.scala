package com.echoed.chamber.services

import com.echoed.chamber.domain.public.StoryPublic
import collection.immutable.TreeMap

trait ContentService {

    implicit object StoryOrdering extends Ordering[(Long, String)] {
        def compare(a:(Long, String), b:(Long, String)) = {
            if((b._1 compare a._1) != 0)
                b._1 compare a._1
            else
                b._2 compare a._2
        }
    }

    val pageSize = 30
    protected var storyMap = Map[String, StoryPublic]()
    protected var storyTree = new TreeMap[(Long, String), StoryPublic]()(StoryOrdering)

    protected def addToTree(s: StoryPublic){
        storyTree += ((s.story.updatedOn, s.story.id) -> s)
    }

    protected def removeFromTree(s: StoryPublic){
        storyTree  -= ((s.story.updatedOn, s.story.id))
    }

    protected def updateStory(s: StoryPublic){
        storyMap.get(s.id).map {
            removeFromTree(_)
        }
        addToTree(s)
        storyMap += (s.id -> s)
    }

    protected def getNextPage(page: Int, list: List[StoryPublic]) = {
        if (page * pageSize + pageSize <= list.length) (page + 1).toString else null
    }

    protected def getStoriesFromTree(page: Int) = {
        val start = page * pageSize
        val stories = storyTree.values.map(s => s.published).toList
        stories.slice(start, start + pageSize)
        stories
    }

    protected def getStoryCount = {
        storyTree.values.map(s => s.published).toList.length
    }

}
