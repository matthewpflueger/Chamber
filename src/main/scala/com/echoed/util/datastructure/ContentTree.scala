package com.echoed.util.datastructure

//import com.echoed.chamber.domain.public.StoryPublic
import collection.immutable.TreeMap
import com.echoed.chamber.domain.views.content.{ContentDescription, Content}

case class ContentTree(c: ContentDescription) {

    implicit object DateOrdering extends Ordering[(Long, String)] {
        def compare(a:(Long, String), b:(Long, String)) = {
            if((b._1 compare a._1) != 0)
                b._1 compare a._1
            else
                b._2 compare a._2
        }
    }


    def getInfoMap(contentPath: Option[String] = None, startsWith: Option[Boolean] = Some(false)) = {
        val everything = contentPath.filterNot(_.isEmpty).isEmpty
        val contentCount = {
            if (everything) count
            else if (startsWith.getOrElse(false)) contentTree.values.filter(_.contentPath.exists(_.startsWith(contentPath.get))).toList.size
            else contentTree.values.filter(_.contentPath == contentPath).toList.size
        }

        Map("name" -> c.plural, "count" -> contentCount, "endPoint" -> c.endPoint)
    }

    val pageSize = 30
    var viewCount = 0
    var voteCount = 0
    var commentCount = 0

    var mostViewed: Option[Content] = None
    var mostCommented: Option[Content] = None
    var mostVoted: Option[Content] = None

    protected var contentMap = Map[String, Content]()
    protected var contentTree = new TreeMap[(Long, String), Content]()(DateOrdering)

    protected def get(id: String) = contentMap.get(id)

    protected def addToTree(c: Content): Unit = contentTree += ((c.updatedOn, c.id) -> c)

    protected def removeFromTree(c: Content): Unit = contentTree -= ((c.updatedOn, c.id))

    def deleteContent(c: Content): Unit =
        contentMap.get(c.id).map { c =>
            viewCount -= c.numViews
            voteCount -= c.numVotes
            commentCount -= c.numComments
            removeFromTree(c)
        }

    def updateContent(c: Content) {
        deleteContent(c)
        addToTree(c)
        contentMap += (c.id -> c)
        viewCount += c.numViews
        voteCount += c.numVotes
        commentCount += c.numComments

        mostViewed = mostViewed.filter(_.numViews > c.numViews).orElse(Option(c))
        mostCommented = mostCommented.filter(_.numComments > c.numComments).orElse(Option(c))
        mostVoted = mostVoted.filter(_.numVotes > c.numVotes).orElse(Option(c))
    }

    def getAllContentFromTree = contentTree.values.toList

    def getContentFromTree(
            contentPath: Option[String] = None,
            startsWith: Option[Boolean] = Some(false),
            page: Option[Int] = Some(0)) = {

        val pg = page.getOrElse(0)
        val start = pg * pageSize
        val everything = contentPath.filterNot(_.isEmpty).isEmpty
        val content = {
            if (everything) contentTree.values
            else if (startsWith.getOrElse(false)) contentTree.values.filter(_.contentPath.exists(_.startsWith(contentPath.get)))
            else contentTree.values.filter(_.contentPath == contentPath)
        }.toList

        val nextPage = if (pg * pageSize + pageSize <= content.size) Some(pg + 1) else None

        if (everything) ContentTreeContext(
                content.slice(start, start + pageSize),
                nextPage,
                commentCount,
                viewCount,
                voteCount,
                mostCommented,
                mostViewed,
                mostVoted)
        else {
            var cc = 0
            var vwc = 0
            var vtc = 0
            var mc: Option[Content] = None
            var mvw: Option[Content] = None
            var mvt: Option[Content] = None

            content.foreach { c =>
                cc += c.numComments
                vwc += c.numViews
                vtc += c.numVotes
                mc = mc.filter(_.numComments > c.numComments).orElse(Option(c))
                mvw = mvw.filter(_.numViews > c.numViews).orElse(Option(c))
                mvt = mvt.filter(_.numVotes > c.numVotes).orElse(Option(c))
            }

            ContentTreeContext(
                content.slice(start, start + pageSize),
                nextPage,
                cc,
                vwc,
                vtc,
                mc,
                mvw,
                mvt)
        }

//        ( content, nextPage )
    }

    def count = contentTree.size

}

case class ContentTreeContext(
        content: List[Content] = List(),
        nextPage: Option[Int] = None,
        commentCount: Int = 0,
        viewCount: Int = 0,
        voteCount: Int = 0,
        mostCommented: Option[Content] = None,
        mostViewed: Option[Content] = None,
        mostVoted: Option[Content] = None) {

    val highlights = List(
        Map("name" -> "Highest Rated", "value" -> mostVoted),
        Map("name" -> "Most Discussed", "value" -> mostCommented),
        Map("name" -> "Most Viewed", "value" -> mostViewed))
}

