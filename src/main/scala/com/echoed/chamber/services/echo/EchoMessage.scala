package com.echoed.chamber.services.echo

import com.echoed.chamber.services.{EchoedException, MessageResponse => MR, Message}
import com.echoed.chamber.domain.views.EchoPossibilityView
import com.echoed.chamber.domain.{EchoClick, Echo}


sealed trait EchoMessage extends Message

sealed case class EchoException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)

import com.echoed.chamber.services.echo.{EchoMessage => EM}
import com.echoed.chamber.services.echo.{EchoException => EE}


@deprecated(message = "Old integration message will be deleted as soon as possible")
case class RecordEchoPossibility(echoPossibility: Echo) extends EM
@deprecated
case class RecordEchoPossibilityResponse(
        message: RecordEchoPossibility,
        value: Either[EE, EchoPossibilityView]) extends EM with MR[EchoPossibilityView, RecordEchoPossibility, EE]

case class EchoExists(
        echoPossibilityView: EchoPossibilityView,
        m: String = "",
        c: Throwable = null) extends EE(m, c)

case class GetEcho(echoPossibilityId: String) extends EM
case class GetEchoResponse(message: GetEcho, value: Either[EE, Echo])
        extends EM with MR[Echo, GetEcho, EE]

case class GetEchoById(echoId: String) extends EM
case class GetEchoByIdResponse(message: GetEchoById, value: Either[EE, Echo])
        extends EM with MR[Echo, GetEchoById, EE]

case class GetEchoByIdAndEchoedUserId(echoId: String, echoedUserId: String) extends EM
case class GetEchoByIdAndEchoedUserIdResponse(message: GetEchoByIdAndEchoedUserId, value: Either[EE, Echo])
        extends EM with MR[Echo, GetEchoByIdAndEchoedUserId, EE]

case class EchoNotFound(id: String, m: String = "Echo not found") extends EE(m)

case class EchoPossibilityNotFound(id: String, m: String = "Echo possibility not found") extends EE(m)


case class GetEchoPossibility(echoPossibilityId: String) extends EM
case class GetEchoPossibilityResponse(message: GetEchoPossibility, value: Either[EE, Echo])
        extends EM with MR[Echo, GetEchoPossibility, EE]

case class RecordEchoClick(echoClick: EchoClick, linkId: String, postId: String) extends EM
case class RecordEchoClickResponse(message: RecordEchoClick, value: Either[EE, Echo])
        extends EM with MR[Echo, RecordEchoClick, EE]
