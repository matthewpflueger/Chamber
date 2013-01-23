package com.echoed.util.datastructure

import com.echoed.chamber.domain.views.content.Content

class ContentManager {

    private var cache = Map[String, ContentTree]()

    def updateContent( c: Content ) {
        val tree = cache.get(c._type).getOrElse(new ContentTree( c._singular, c._plural, c._endPoint))
        tree.updateContent(c)
        cache += (c._type -> tree)
    }

    def getContentList = {
        cache.values.map {
            t => Map( "name" -> t.plural, "count" -> t.count, "endPoint" -> t.endPoint )
        }.toList
    }

    def getContent(_type: String, page: Int) = {
        cache.get(_type).map(_.getContentFromTree(page)).getOrElse((List[Content](), null))
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
        s = Map("name" -> "Highest Rated", "value" -> getMostVoted("story")) :: s
        s = Map("name" -> "Most Discussed", "value" -> getMostCommented("story")) :: s
        s = Map("name" -> "Most Viewed", "value" -> getMostViewed("story")) :: s
        s
    }

    def getStats = {
        var s = List[Map[String, Any]]()
        s = Map("name" -> "Votes", "value" -> getTotalVoteCount) :: s
        s = Map("name" -> "Comments", "value" -> getTotalCommentCount) :: s
        s = Map("name" -> "Views", "value" -> getTotalViewCount) :: s
        s
    }

    def getMostViewed( _type: String ) = {
        cache.get( _type ).map(_.mostViewed).getOrElse(null)
    }

    def getMostCommented( _type: String ) = {
        cache.get( _type ).map(_.mostCommented).getOrElse(null)
    }

    def getMostVoted( _type: String ) = {
        cache.get( _type ).map(_.mostVoted).getOrElse(null)
    }


}