package com.echoed.chamber.domain

case class ModerationDescription(
        moderated: Boolean,
        moderatedBy: Option[String],
        moderatedOn: Option[Long],
        echoedModerated: Boolean,
        echoedModeratedBy: Option[String],
        echoedModeratedOn: Option[Long],
        selfModerated: Boolean,
        selfModeratedBy: Option[String],
        selfModeratedOn: Option[Long]) {

    def this(
            moderation: Option[Moderation] = None,
            echoedModeration: Option[Moderation] = None,
            selfModeration: Option[Moderation] = None) = this(
        moderation.map(_.moderated).getOrElse(false),
        moderation.map(_.moderatedBy),
        moderation.map(_.createdOn),
        echoedModeration.map(_.moderated).getOrElse(false),
        echoedModeration.map(_.moderatedBy),
        echoedModeration.map(_.createdOn),
        selfModeration.map(_.moderated).getOrElse(false),
        selfModeration.map(_.moderatedBy),
        selfModeration.map(_.createdOn))
}
