package com.echoed.chamber.services.geolocation

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain.GeoLocation


sealed trait GeoLocationMessage extends Message

sealed case class GeoLocationException(ipAddress: String, message: String = "", cause: Throwable = null) extends EchoedException(message, cause)

import com.echoed.chamber.services.geolocation.{GeoLocationMessage => GLM}
import com.echoed.chamber.services.geolocation.{GeoLocationException => GLE}


private[geolocation] case class FindForCrawl() extends GLM


case class GeoLocate(gl: GeoLocation) extends GLM
case class GeoLocateResponse(message: GeoLocate, value: Either[GLE, GeoLocation])
        extends GLM with RM[GeoLocation, GeoLocate, GLE]

case class MalformedResponse(ip: String, c: Throwable, m: String = "Received malformed response") extends GLE(ip, m, c)
//case class AssignFacebookServiceResponse(message: AssignFacebookService, value: Either[EUE, FacebookService])
//        extends EUM with RM[FacebookService, AssignFacebookService, EUE]
