package com.echoed.util.datastructure

//import com.echoed.chamber.domain.public.StoryPublic
import com.echoed.chamber.domain.public.Content
import collection.immutable.TreeMap

case class ContentTree( singular: String, plural: String, endPoint: String ) {

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

    var mostViewed: Content = null
    var mostCommented: Content = null
    var mostVoted: Content = null

    protected var contentMap = Map[String, Content]()
    protected var contentTree =  new TreeMap[(Long, String), Content]()(DateOrdering)

    protected def get(id: String) = {
        contentMap.get(id)
    }

    protected def addToTree(c: Content){
        contentTree += ((c._updatedOn, c._id) -> c)
    }

    protected def removeFromTree(c: Content){
        contentTree -= ((c._updatedOn, c._id))
    }

    def updateContent(c: Content){
        contentMap.get(c._id).map {
            story =>
                viewCount -= c._views
                voteCount -= c._votes
                commentCount -= c._comments
                removeFromTree(story)
        }
        addToTree(c)
        contentMap += (c._id -> c)
        viewCount += c._views
        voteCount += c._votes
        commentCount += c._comments

        Option(mostViewed).map {
            content => if( content._views <= c._views ) mostViewed = c
        }.getOrElse {
            mostViewed = c
        }

        Option(mostCommented).map {
            content =>  if(content._comments <= c._views ) mostCommented = c
        }.getOrElse {
            mostCommented = c
        }

        Option(mostVoted).map {
            content => if (content._votes <= c._votes) mostVoted = c
        }.getOrElse {
            mostVoted = c
        }
    }

    def getNextPage(page: Int) = {
        if (page * pageSize + pageSize <= contentTree.size) (page + 1).toString else null
    }

    def getContentFromTree(page: Int) = {
        val start = page * pageSize
        val stories = contentTree.values.toList.slice(start, start + pageSize)
        val nextPage = getNextPage(page)
        ( stories, nextPage )
    }

    def count = {
        contentTree.values.toList.length
    }
}
