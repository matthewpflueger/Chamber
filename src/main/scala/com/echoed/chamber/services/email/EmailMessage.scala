package com.echoed.chamber.services.email

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import java.util.{Map => JMap}


sealed trait EmailMessage extends Message

sealed case class EmailException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)

import com.echoed.chamber.services.email.{EmailMessage => EM}
import com.echoed.chamber.services.email.{EmailException => EE}


case class SendEmail(recipient: String, subject: String, view: String, model: JMap[String, AnyRef]) extends EM
case class SendEmailResponse(
        message: SendEmail,
        value: Either[EE, Boolean]) extends EM with RM[Boolean, SendEmail, EE]

