package com.echoed.chamber.services.echo

import com.echoed.chamber.domain.views.EchoFull
import com.echoed.chamber.services.{Message, ResponseMessage, EchoedException}


case class EchoResponseMessage(
        message: EchoRequestMessage,
        value: Either[EchoedException, EchoFull])
        extends Message
        with ResponseMessage[EchoFull, EchoRequestMessage, EchoedException]


