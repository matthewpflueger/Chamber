package com.echoed.chamber.domain.views.content

trait Content extends FeedItem {

    def updatedOn:     Long
    def createdOn:     Long

    def numViews:      Int
    def numVotes:      Int
    def numComments:   Int

    def contentDescription = Content.getContentDescription(contentType)
    def contentPath:  Option[String] = None
}

object Content {
    val discussionContentDescription =  new ContentDescription("Discussion", "General Discussion", "discussions", 1)
    val storyContentDescription =       new ContentDescription("Story", "Stories" , "stories", 2)
    val qaContentDescription =          new ContentDescription("Question", "Q & A", "questions", 3)
    val reviewContentDescription =      new ContentDescription("Review", "Reviews" , "reviews", 4)
    val dealContentDescription =        new ContentDescription("Deal", "Deals", "deals", 5)
    val newsContentDescription =        new ContentDescription("News", "News", "news", 6)
    val photoContentDescription =       new ContentDescription("Photo", "Photos", "photos", 10)

    val defaultContentDescriptions =    List(
            discussionContentDescription,
            qaContentDescription,
            storyContentDescription,
            reviewContentDescription)


    val defaultContentDescription = discussionContentDescription

    val allContentDescriptions = photoContentDescription :: newsContentDescription :: defaultContentDescriptions

    def getContentDescription(contentType: String) = allContentDescriptions
            .filter(_.isContentDescription(contentType))
            .headOption
            .getOrElse(defaultContentDescription)
}

case class ContentDescription(singular: String, plural: String, endPoint: String, ordering: Int) {
    def isContentDescription(contentType: String) =
            singular == contentType || plural == contentType || endPoint == contentType
}
