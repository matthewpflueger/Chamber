package com.echoed.chamber.domain.public

import com.echoed.chamber.domain._
import java.util.{List => JList}
import com.echoed.chamber.domain.Chapter
import com.echoed.chamber.domain.Story
import com.echoed.chamber.domain.ChapterImage
import views.content.{FeedItem, Content}
import views.StoryFull
import scala.collection.JavaConversions._

case class StoryPublic (
        id: String,
        story: Story,
        echoedUser: EchoedUserPublic,
        partner: PartnerPublic,
        chapters: JList[Chapter],
        chapterImages: JList[ChapterImage],
        comments: List[CommentPublic],
        votes: Map[String, Vote],
        moderation: ModerationDescription,
        topic: TopicPublic) extends Content {

    val contentType =  story.contentType
    override val contentPath =  story.contentPath
    val title =        story.title.orNull

    def createdOn =    story.createdOn
    def updatedOn =    story.updatedOn
    def numViews =     story.views
    def numVotes =     votes.values.toList.foldLeft(0)((l, r) => l + r.value)
    def numComments =  comments.length

    def this(story: StoryFull) = this(
        story.id,
        story.story,
        new EchoedUserPublic(story.echoedUser),
        new PartnerPublic(story.partner),
        story.chapters,
        story.chapterImages,
        story.convertCommentsToPublic,
        story.votes,
        story.moderation,
        Option(story.topic).map(new TopicPublic(_)).orNull)

    def published = this.copy(chapters = chapters.filter(c => c.publishedOn != 0))

    val isPublished = chapters.foldLeft(false)(_ || _.publishedOn != 0)

    def isModerated = moderation.moderated
    def isEchoedModerated = moderation.echoedModerated
    def isSelfModerated = moderation.selfModerated

    def isOwnedBy(id: String) = echoedUser.id == id || echoedUser.screenName == id

    def voteScore = votes.values.toList.foldLeft(0)((l, r) => l + r.value)

    def extractImages = {
        val images = chapterImages.map(_.image).toList
        Option(story.image).map(_ :: images).getOrElse(images)
    }

    def storyUpdatedNotification = new Notification(
        echoedUser,
        "story updated",
        Map(
            "subject" -> echoedUser.name,
            "action" -> "updated story",
            "object" -> Option(title).getOrElse("with no title"),
            "storyId" -> id,
            "partnerId" -> partner.id,
            "partnerName" -> partner.name))

}