package com.echoed.util.datastructure

import com.echoed.chamber.domain.public.StoryPublic
import collection.immutable.TreeMap

class ContentTree {

    implicit object DateOrdering extends Ordering[(Long, String)] {
        def compare(a:(Long, String), b:(Long, String)) = {
            if((b._1 compare a._1) != 0)
                b._1 compare a._1
            else
                b._2 compare a._2
        }
    }

    val pageSize = 30
    var viewCount = 0
    var voteCount = 0
    var commentCount = 0

    var mostViewed: StoryPublic = null
    var mostCommented: StoryPublic = null

    protected var contentMap = Map[String, StoryPublic]()
    protected var contentTree =  new TreeMap[(Long, String), StoryPublic]()(DateOrdering)

    protected def get(id: String) = {
        contentMap.get(id)
    }

    protected def addToTree(s: StoryPublic){
        contentTree += ((s.story.updatedOn, s.story.id) -> s)
    }

    protected def removeFromTree(s: StoryPublic){
        contentTree  -= ((s.story.updatedOn, s.story.id))
    }

    def updateStory(s: StoryPublic){
        contentMap.get(s.id).map {
            story =>
                viewCount -= story.story.views
                voteCount -= story.votes.size
                commentCount -= s.comments.size
                removeFromTree(story)
        }
        addToTree(s)
        contentMap += (s.id -> s)
        viewCount += s.story.views
        voteCount += s.votes.size
        commentCount += s.comments.size

        Option(mostViewed).map {
            sp =>
                if(sp.story.views <= s.story.views) mostViewed = s
        }.getOrElse{
            mostViewed = s
        }

        Option(mostCommented).map {
            sp =>
                if(sp.comments.size <= s.comments.size) mostCommented = s
        }.getOrElse {
            mostCommented = s
        }

    }

    def getNextPage(page: Int) = {
        if (page * pageSize + pageSize <= contentTree.size) (page + 1).toString else null
    }

    def getContentFromTree(page: Int) = {
        val start = page * pageSize
        val stories = contentTree.values.map(s => s.published).toList.slice(start, start + pageSize)
        stories
    }

    def count = {
        contentTree.values.map(s => s.published).toList.length
    }
}
