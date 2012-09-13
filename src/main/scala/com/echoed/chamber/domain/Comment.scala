package com.echoed.chamber.domain

import java.util.Date
import com.echoed.util.DateUtils._
import com.echoed.util.UUID


case class Comment(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        storyId: String,
        echoedUserId: String,
        partnerId: String,
        partnerHandle: String,
        partnerSettingsId: String,
        echoId: String,
        chapterId: String,
        byEchoedUserId: String,
        echoedUser: EchoedUser,
        parentCommentId: String,
        text: String) extends DomainObject {

    def this() = this(
        id = "",
        updatedOn = 0L,
        createdOn = 0L,
        storyId = "",
        echoedUserId = "",
        partnerId = "",
        partnerHandle = "",
        partnerSettingsId = "",
        echoId = "",
        chapterId = "",
        byEchoedUserId = "",
        echoedUser = new EchoedUser(),
        parentCommentId = "",
        text = "")

    def this(
            chapter: Chapter,
            byEchoedUser: EchoedUser,
            _text: String,
            parentComment: Option[Comment] = None) = this(
        UUID(),
        new Date,
        new Date,
        storyId = chapter.storyId,
        echoedUserId = chapter.echoedUserId,
        partnerId = chapter.partnerId,
        partnerHandle = chapter.partnerHandle,
        partnerSettingsId = chapter.partnerSettingsId,
        echoId = chapter.echoId,
        chapterId = chapter.id,
        byEchoedUserId = byEchoedUser.id,
        echoedUser = byEchoedUser,
        parentCommentId = parentComment.map(_.id).orNull,
        text = _text)

}
