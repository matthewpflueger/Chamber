package com.echoed.chamber.domain.views

import com.echoed.chamber.domain._

import java.util.{ArrayList, List => JList}
import scala.collection.JavaConversions
import com.echoed.chamber.domain.public.CommentPublic
import java.util

case class StoryFull(
        id: String,
        story: Story,
        echoedUser: EchoedUser,
        chapters: JList[Chapter],
        chapterImages: JList[ChapterImage],
        comments: JList[Comment],
        votes: JList[Vote],
        moderation: Option[ModerationDescription] = None) {

    def this(id:String, story: Story, echoedUser: EchoedUser) = this(
            id,
            story,
            echoedUser,
            new ArrayList[Chapter],
            new ArrayList[ChapterImage],
            new ArrayList[Comment],
            new util.ArrayList[Vote])

    def convertCommentsToPublic = JavaConversions.asScalaBuffer(comments).map({ new CommentPublic(_) }).toList
}

