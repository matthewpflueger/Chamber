package com.echoed.chamber.domain.views

import com.echoed.chamber.domain._

import partner.Partner
import com.echoed.chamber.domain.public.CommentPublic

case class StoryFull(
        id: String,
        story: Story,
        echoedUser: EchoedUser,
        partner: Partner,
        chapters: List[Chapter],
        chapterImages: List[ChapterImage],
        comments: List[Comment],
        links: List[Link],
        votes: Map[String, Vote],
        moderation: ModerationDescription,
        topic: Topic) {

    def this(id:String, story: Story, echoedUser: EchoedUser, partner: Partner) = this(
            id,
            story,
            echoedUser,
            partner,
            List[Chapter](),
            List[ChapterImage](),
            List[Comment](),
            List[Link](),
            Map.empty[String, Vote],
            new ModerationDescription(),
            null)

    def convertCommentsToPublic = comments.map(new CommentPublic(_)).toList

    val isNew = Option(story).map(_.createdOn).filter(_ > 0).isEmpty
}

