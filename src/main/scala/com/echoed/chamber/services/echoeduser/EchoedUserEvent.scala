package com.echoed.chamber.services.echoeduser

import com.echoed.chamber.services.{UpdatedEvent, CreatedEvent, Event}
import com.echoed.chamber.domain.{Notification, TwitterUser, FacebookUser, EchoedUser}


trait EchoedUserEvent extends Event

import com.echoed.chamber.services.echoeduser.{EchoedUserEvent => EUE}


case class StoryUpdated(storyId: String) extends EUE with UpdatedEvent

private[services] case class EchoedUserCreated(
                echoedUser: EchoedUser,
                facebookUser: Option[FacebookUser] = None,
                twitterUser: Option[TwitterUser] = None) extends EUE with CreatedEvent

private[services] case class EchoedUserUpdated(
                echoedUser: EchoedUser,
                facebookUser: Option[FacebookUser],
                twitterUser: Option[TwitterUser]) extends EUE with UpdatedEvent

private[services] case class NotificationCreated(notification: Notification) extends EUE with CreatedEvent

private[services] case class NotificationUpdated(notification: Notification) extends EUE with UpdatedEvent


