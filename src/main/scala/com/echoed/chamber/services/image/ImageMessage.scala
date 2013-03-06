package com.echoed.chamber.services.image

import com.echoed.chamber.services.{EchoedException, MessageResponse => MR, Message}
import com.echoed.chamber.domain.Link

sealed trait ImageMessage extends Message

sealed case class ImageException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)

import com.echoed.chamber.services.image.{ImageMessage => IM}
import com.echoed.chamber.services.image.{ImageException => IE}


case class Capture(link: Link) extends IM
case class CaptureResponse(
        message: Capture,
        value: Either[IE, Link]) extends IM with MR[Link, Capture, IE]

