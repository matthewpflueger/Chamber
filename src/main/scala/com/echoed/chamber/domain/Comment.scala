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
        echoedUser: EchoedUser,
        parentCommentId: String,
        text: String) extends DomainObject {

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
        echoedUser = byEchoedUser,
        parentCommentId = parentComment.map(_.id).orNull,
        text = _text)

}
