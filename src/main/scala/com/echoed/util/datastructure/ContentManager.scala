package com.echoed.util.datastructure

import com.echoed.chamber.domain.views.content.{ContentDescription, Content}
import com.echoed.chamber.domain.public.StoryPublic
import com.echoed.chamber.domain.Story

class ContentManager(defaultContentDescriptions: List[ContentDescription]) {

    private var cache = Map[ContentDescription, ContentTree]()

    defaultContentDescriptions.map(initContentTree(_))

    def this() = this(List())

    def initContentTree(c: ContentDescription) =  {
        val tree = cache.get(c).getOrElse(new ContentTree(c))
        cache += (c -> tree)
        tree
    }


    def updateContent( c: Content ) {
        val tree = initContentTree(c.contentDescription)
        tree.updateContent(c)
        cache += (c.contentDescription -> tree)
    }

    def getContentList = {
        cache.values.map(_.getInfoMap).toList
    }

    def getContent(c: ContentDescription, page: Int) = {
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
        s = Map("name" -> "Highest Rated", "value" -> getMostVoted(Story.storyContentDescription)) :: s
        s = Map("name" -> "Most Discussed", "value" -> getMostCommented(Story.storyContentDescription)) :: s
        s = Map("name" -> "Most Viewed", "value" -> getMostViewed(Story.storyContentDescription)) :: s
        s
    }

    def getStats = {
        var s = List[Map[String, Any]]()
        s = Map("name" -> "Votes", "value" -> getTotalVoteCount) :: s
        s = Map("name" -> "Comments", "value" -> getTotalCommentCount) :: s
        s = Map("name" -> "Views", "value" -> getTotalViewCount) :: s
        s
    }

    def getMostViewed( c: ContentDescription ) = {
        cache.get(c).map(_.mostViewed).getOrElse(null)
    }

    def getMostCommented( c: ContentDescription) = {
        cache.get(c).map(_.mostCommented).getOrElse(null)
    }

    def getMostVoted( c: ContentDescription ) = {
        cache.get(c).map(_.mostVoted).getOrElse(null)
    }


}