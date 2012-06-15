package com.echoed.chamber.domain

import java.util.{UUID, Date}
import com.echoed.util.DateUtils._


case class Comment(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        storyId: String,
        echoedUserId: String,
        partnerId: String,
        partnerSettingsId: String,
        echoId: String,
        chapterId: String,
        byEchoedUserId: String,
        parentCommentId: String,
        text: String) {

    def this(
            chapter: Chapter,
            byEchoedUser: EchoedUser,
            _text: String,
            parentComment: Option[Comment] = None) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        storyId = chapter.storyId,
        echoedUserId = chapter.echoedUserId,
        partnerId = chapter.partnerId,
        partnerSettingsId = chapter.partnerSettingsId,
        echoId = chapter.echoId,
        chapterId = chapter.id,
        byEchoedUserId = byEchoedUser.id,
        parentCommentId = parentComment.map(_.id).orNull,
        text = _text)

}
