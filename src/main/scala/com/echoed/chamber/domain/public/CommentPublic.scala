package com.echoed.chamber.domain.public

import com.echoed.chamber.domain.Comment

case class CommentPublic(
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
    echoedUser: EchoedUserPublic,
    parentCommentId: String,
    text: String) {

    def this(comment: Comment) = this(
        comment.id,
        comment.updatedOn,
        comment.createdOn,
        comment.storyId,
        comment.echoedUserId,
        comment.partnerId,
        comment.partnerHandle,
        comment.partnerSettingsId,
        comment.echoId,
        comment.chapterId,
        new EchoedUserPublic(comment.echoedUser),
        comment.parentCommentId,
        comment.text
    )

}
