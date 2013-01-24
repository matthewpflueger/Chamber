package com.echoed.util.datastructure

//import com.echoed.chamber.domain.public.StoryPublic
import collection.immutable.TreeMap
import com.echoed.chamber.domain.views.content.Content

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
        contentTree += ((c.updatedOn, c.id) -> c)
    }

    protected def removeFromTree(c: Content){
        contentTree -= ((c.updatedOn, c.id))
    }

    def updateContent(c: Content){
        contentMap.get(c.id).map {
            story =>
                viewCount -= c.numViews
                voteCount -= c.numVotes
                commentCount -= c.numComments
                removeFromTree(story)
        }
        addToTree(c)
        contentMap += (c.id -> c)
        viewCount += c.numViews
        voteCount += c.numVotes
        commentCount += c.numComments

        Option(mostViewed).map {
            content => if( content.numViews <= c.numViews ) mostViewed = c
        }.getOrElse {
            mostViewed = c
        }

        Option(mostCommented).map {
            content =>  if(content.numComments <= c.numComments ) mostCommented = c
        }.getOrElse {
            mostCommented = c
        }

        Option(mostVoted).map {
            content => if (content.numVotes <= c.numVotes) mostVoted = c
        }.getOrElse {
            mostVoted = c
        }
    }

    def getNextPage(page: Int) = {
        if (page * pageSize + pageSize <= contentTree.size) (page + 1).toString else null
    }

    def getAllContentFromTree = {
        contentTree.values.toList
    }

    def getContentFromTree(page: Int) = {
        val start = page * pageSize
        val content = contentTree.values.toList.slice(start, start + pageSize)
        val nextPage = getNextPage(page)
        ( content, nextPage )
    }

    def count = {
        contentTree.values.toList.length
    }
}
