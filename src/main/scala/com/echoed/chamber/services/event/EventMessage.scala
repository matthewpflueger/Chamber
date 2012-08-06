package com.echoed.chamber.services.event

import com.echoed.chamber.services.{EchoedException, MessageResponse => MR, Message}
import com.echoed.chamber.domain.EchoedUser


sealed trait EventMessage extends Message

sealed case class EventException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)

import com.echoed.chamber.services.event.{EventMessage => EM}
import com.echoed.chamber.services.event.{EventException => EE}

private[event] abstract case class Event(
        name: String,
        ref: String,
        refId: String) extends EM

case class FacebookCanvasViewed(echoedUser: EchoedUser)
        extends Event("FacebookCanvasViewed", echoedUser.getClass.getSimpleName, echoedUser.id)

case class ExhibitViewed(echoedUser: EchoedUser)
        extends Event("ExhibitViewed", echoedUser.getClass.getSimpleName, echoedUser.id)

case class WidgetRequested(partnerId: String)
        extends Event("WidgetRequested", "PartnerId", partnerId)

case class WidgetOpened(partnerId: String)
        extends Event("WidgetOpened", "PartnerId", partnerId)

case class WidgetStoryOpened(storyId: String)
        extends Event("WidgetStoryOpened", "StoryId", storyId)


