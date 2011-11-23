package com.echoed.chamber.services

abstract case class ResponseMessage(
        requestMessage: Message,
        errorMessage: Option[ErrorMessage] = None) extends Message(
            version = requestMessage.version,
            id = requestMessage.id,
            correlation = Option(requestMessage))









