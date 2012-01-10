package com.echoed.chamber.services.echo

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain.EchoPossibility
import com.echoed.chamber.domain.views.EchoPossibilityView


sealed trait EchoMessage extends Message

sealed case class EchoException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)

import com.echoed.chamber.services.echo.{EchoMessage => EM}
import com.echoed.chamber.services.echo.{EchoException => EE}


case class RecordEchoPossibility(echoPossibility: EchoPossibility) extends EM
case class RecordEchoPossibilityResponse(
        message: RecordEchoPossibility,
        value: Either[EE, EchoPossibilityView]) extends EM with RM[EchoPossibilityView, RecordEchoPossibility, EE]

case class EchoExistsException(
        echoPossibilityView: EchoPossibilityView,
        m: String = "",
        c: Throwable = null) extends EE(m, c)
