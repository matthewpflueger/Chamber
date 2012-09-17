package com.echoed.chamber.domain.public

import com.echoed.chamber.domain._
import java.util.{List => JList}
import com.echoed.chamber.domain.Chapter
import com.echoed.chamber.domain.Story
import com.echoed.chamber.domain.ChapterImage
import views.StoryFull

case class StoryPublic(
        id: String,
        story: Story,
        echoedUser: EchoedUserPublic,
        chapters: JList[Chapter],
        chapterImages: JList[ChapterImage],
        comments: List[CommentPublic],
        moderation: Option[ModerationDescription] = None) {

    def this(story: StoryFull) = this(
        story.id,
        story.story,
        new EchoedUserPublic(story.echoedUser),
        story.chapters,
        story.chapterImages,
        story.convertCommentsToPublic,
        story.moderation)

    def isModerated = moderation.map(_.moderated).getOrElse(false)
    def isEchoedModerated = moderation.map(_.echoedModerated).getOrElse(false)
}
