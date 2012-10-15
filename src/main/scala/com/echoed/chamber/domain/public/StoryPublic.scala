package com.echoed.chamber.domain.public

import com.echoed.chamber.domain._
import java.util.{List => JList}
import com.echoed.chamber.domain.Chapter
import com.echoed.chamber.domain.Story
import com.echoed.chamber.domain.ChapterImage
import views.StoryFull
import scala.collection.JavaConversions._

case class StoryPublic(
        id: String,
        story: Story,
        echoedUser: EchoedUserPublic,
        chapters: JList[Chapter],
        chapterImages: JList[ChapterImage],
        comments: List[CommentPublic],
        votes: Map[String, Vote],
        moderation: ModerationDescription) {

    def this(story: StoryFull) = this(
        story.id,
        story.story,
        new EchoedUserPublic(story.echoedUser),
        story.chapters,
        story.chapterImages,
        story.convertCommentsToPublic,
        story.votes,
        story.moderation)

    def published = this.copy(chapters = chapters.filter(c => c.publishedOn != 0))

    def isPublished = chapters.foldLeft(false)(_ || _.publishedOn != 0)

    def isModerated = moderation.moderated
    def isEchoedModerated = moderation.echoedModerated
    def isSelfModerated = moderation.selfModerated
}
