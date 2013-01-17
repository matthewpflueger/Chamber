package com.echoed.util.datastructure

import com.echoed.chamber.domain.public.Content

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

    def getTotalViewCount = {
        cache.values.foldLeft(0)((l, r) => l + r.viewCount)
    }

    def getTotalCommentCount = {
        cache.values.foldLeft(0)((l, r) => l + r.commentCount)
    }

    def getTotalVoteCount = {
        cache.values.foldLeft(0)((l, r) => l + r.voteCount)
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