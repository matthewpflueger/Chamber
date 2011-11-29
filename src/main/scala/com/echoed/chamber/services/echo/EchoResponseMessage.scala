package com.echoed.chamber.services.echo

import com.echoed.chamber.domain.views.EchoFull
import com.echoed.chamber.services.{ResponseMessage, ErrorMessage}


case class EchoResponseMessage(
        echoRequestMessage: EchoRequestMessage,
        echoValue: Either[ErrorMessage, EchoFull]) extends ResponseMessage(
            requestMessage = echoRequestMessage,
            value = echoValue)

