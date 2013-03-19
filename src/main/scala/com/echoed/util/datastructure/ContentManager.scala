package com.echoed.util.datastructure

import com.echoed.chamber.domain.views.content.{ContentDescription, Content}
import collection.immutable.TreeMap

class ContentManager(defaultContentDescriptions: List[ContentDescription], defaultContentDescription: ContentDescription = Content.discussionContentDescription) {

    implicit object ContentOrdering extends Ordering[ContentDescription] {
        def compare(a: ContentDescription, b: ContentDescription) = {
            a.ordering compare b.ordering
        }
    }

    private var cache =  TreeMap[ContentDescription, ContentTree]()(ContentOrdering)

    defaultContentDescriptions.map(initContentTree(_))

    def this() = this(List())

    def initContentTree(c: ContentDescription) =
        cache.get(c).getOrElse {
            val tree = new ContentTree(c)
            cache += (c -> tree)
            tree
        }

    def deleteContent(c: Content): Unit = cache.get(c.contentDescription).foreach(_.deleteContent(c))

    def updateContent(c: Content): Unit = initContentTree(c.contentDescription).updateContent(c)

    def getContentList(contentPath: Option[String] = None, startsWith: Option[Boolean] = None) = cache.values.map(_.getInfoMap(contentPath, startsWith)).toList

    def getContentList = cache.values.map(_.getInfoMap()).toList

    def getContent(
            c: ContentDescription,
            page: Option[Int] = Some(0),
            contentPath: Option[String] = None,
            startsWith: Option[Boolean] = Some(false)) = {
        cache.get(c).map(_.getContentFromTree(contentPath, startsWith, page)).getOrElse(ContentTreeContext())
    }

    def getDefaultContent(page: Int) = {
        getContent(defaultContentDescription, Option(page))
    }

    def getDefaultContentType = {
        defaultContentDescription
    }

    def getAllContent = cache.values.foldLeft(List[Content]())((l, r) => r.getAllContentFromTree ::: l)

    def getTotalViewCount = cache.values.foldLeft(0)((l, r) => l + r.viewCount)

    def getTotalCommentCount = cache.values.foldLeft(0)((l, r) => l + r.commentCount)

    def getTotalVoteCount = cache.values.foldLeft(0)((l, r) => l + r.voteCount)

    def getHighlights = List(
            Map("name" -> "Highest Rated", "value" -> getMostVoted(Content.storyContentDescription)),
            Map("name" -> "Most Discussed", "value" -> getMostCommented(Content.storyContentDescription)),
            Map("name" -> "Most Viewed", "value" -> getMostViewed(Content.storyContentDescription)))

    def getStats = List(
            Map("name" -> "Votes", "value" -> getTotalVoteCount),
            Map("name" -> "Comments", "value" -> getTotalCommentCount),
            Map("name" -> "Views", "value" -> getTotalViewCount))

    def getMostViewed(c: ContentDescription) = cache.get(c).map(_.mostViewed)

    def getMostCommented(c: ContentDescription) = cache.get(c).map(_.mostCommented)

    def getMostVoted( c: ContentDescription ) = cache.get(c).map(_.mostVoted)
}