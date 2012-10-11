package com.echoed.chamber.domain.views

import com.echoed.chamber.domain._

import java.util.{ArrayList, List => JList}
import scala.collection.JavaConversions._
import com.echoed.chamber.domain.public.CommentPublic

case class StoryFull(
        id: String,
        story: Story,
        echoedUser: EchoedUser,
        chapters: JList[Chapter],
        chapterImages: JList[ChapterImage],
        comments: JList[Comment],
        votes: Map[String, Vote],
        moderation: Option[ModerationDescription] = None) {

    def this(id:String, story: Story, echoedUser: EchoedUser) = this(
            id,
            story,
            echoedUser,
            new ArrayList[Chapter],
            new ArrayList[ChapterImage],
            new ArrayList[Comment],
            Map.empty[String, Vote])

    def convertCommentsToPublic = comments.map(new CommentPublic(_)).toList
}

