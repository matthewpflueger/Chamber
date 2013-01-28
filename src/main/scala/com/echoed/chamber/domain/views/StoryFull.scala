package com.echoed.chamber.domain.views

import com.echoed.chamber.domain._

import java.util.{ArrayList, List => JList}
import partner.Partner
import scala.collection.JavaConversions._
import com.echoed.chamber.domain.public.CommentPublic

case class StoryFull(
        id: String,
        story: Story,
        echoedUser: EchoedUser,
        partner: Partner,
        chapters: JList[Chapter],
        chapterImages: JList[ChapterImage],
        comments: JList[Comment],
        votes: Map[String, Vote],
        moderation: ModerationDescription,
        topic: Topic) {

    def this(id:String, story: Story, echoedUser: EchoedUser, partner: Partner) = this(
            id,
            story,
            echoedUser,
            partner,
            new ArrayList[Chapter],
            new ArrayList[ChapterImage],
            new ArrayList[Comment],
            Map.empty[String, Vote],
            new ModerationDescription(),
            null)

    def convertCommentsToPublic = comments.map(new CommentPublic(_)).toList

    val isNew = Option(story).map(_.createdOn).filter(_ > 0).isEmpty
}

