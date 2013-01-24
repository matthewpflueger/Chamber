package com.echoed.util.datastructure

import com.echoed.chamber.domain.views.content.Content
import com.echoed.chamber.domain.public.StoryPublic

class ContentManager {

    private var cache = Map[Class[_], ContentTree]()

    def updateContent( c: Content ) {
        val tree = cache.get(c.getClass).getOrElse(new ContentTree( c.singular, c.plural, c.endPoint))
        tree.updateContent(c)
        cache += (c.getClass -> tree)
    }

    def getContentList = {
        cache.values.map {
            t => Map( "name" -> t.plural, "count" -> t.count, "endPoint" -> t.endPoint )
        }.toList
    }

    def getContent(c: Class[_], page: Int) = {
        cache.get(c).map(_.getContentFromTree(page)).getOrElse((List[Content](), null))
    }

    def getAllContent = {
        cache.values.foldLeft(List[Content]())((l, r) => r.getAllContentFromTree ::: l)
    }

    def getTotalViewCount = {
        cache.values.foldLeft(0)((l, r) => l + r.viewCount)
    }

    def getTotalCommentCount = {
        cache.values.foldLeft(0)((l, r) => l + r.commentCount)
    }

    def getTotalVoteCount = {
        cache.values.foldLeft(0)((l, r) => l + r.voteCount)
    }

    def getHighlights = {
        var s = List[Map[String, Any]]()
        s = Map("name" -> "Highest Rated", "value" -> getMostVoted(classOf[StoryPublic])) :: s
        s = Map("name" -> "Most Discussed", "value" -> getMostCommented(classOf[StoryPublic])) :: s
        s = Map("name" -> "Most Viewed", "value" -> getMostViewed(classOf[StoryPublic])) :: s
        s
    }

    def getStats = {
        var s = List[Map[String, Any]]()
        s = Map("name" -> "Votes", "value" -> getTotalVoteCount) :: s
        s = Map("name" -> "Comments", "value" -> getTotalCommentCount) :: s
        s = Map("name" -> "Views", "value" -> getTotalViewCount) :: s
        s
    }

    def getMostViewed( c: Class[_] ) = {
        cache.get(c).map(_.mostViewed).getOrElse(null)
    }

    def getMostCommented( c: Class[_]) = {
        cache.get(c).map(_.mostCommented).getOrElse(null)
    }

    def getMostVoted( c: Class[_] ) = {
        cache.get(c).map(_.mostVoted).getOrElse(null)
    }


}