package com.echoed.chamber.domain.public

import com.echoed.chamber.domain._
import java.util.{List => JList}
import com.echoed.chamber.domain.Chapter
import com.echoed.chamber.domain.Story
import com.echoed.chamber.domain.ChapterImage
import views.StoryFull
import scala.collection.JavaConversions._

case class StoryPublic (
        id: String,
        story: Story,
        echoedUser: EchoedUserPublic,
        chapters: JList[Chapter],
        chapterImages: JList[ChapterImage],
        comments: List[CommentPublic],
        votes: Map[String, Vote],
        moderation: ModerationDescription,
        topic: TopicPublic )  extends Content {

    val _type =         "story"

    def _createdOn =    story.createdOn
    def _id =           id
    def _updatedOn =    story.updatedOn
    def _views =        story.views
    def _comments =     comments.size
    def _votes =        votes.values.toList.foldLeft(0)((l, r) => l + r.value)
    def _plural =       "Stories"
    def _singular =     "Story"
    def _endPoint =     "stories"



    def this(story: StoryFull) = this(
        story.id,
        story.story,
        new EchoedUserPublic(story.echoedUser),
        story.chapters,
        story.chapterImages,
        story.convertCommentsToPublic,
        story.votes,
        story.moderation,
        Option(story.topic).map(new TopicPublic(_)).orNull)

    def published = this.copy(chapters = chapters.filter(c => c.publishedOn != 0))

    def isPublished = chapters.foldLeft(false)(_ || _.publishedOn != 0)

    def isModerated = moderation.moderated
    def isEchoedModerated = moderation.echoedModerated
    def isSelfModerated = moderation.selfModerated

    def isOwnedBy(id: String) = echoedUser.id == id || echoedUser.screenName == id

    def voteScore = votes.values.toList.foldLeft(0)((l, r) => l + r.value)

    def extractImages = {
        chapterImages.map{ _.image }.toList
    }

}
