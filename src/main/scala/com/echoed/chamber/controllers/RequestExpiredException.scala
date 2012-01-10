package com.echoed.chamber.controllers

import com.echoed.chamber.services.EchoedException

case class RequestExpiredException(
        message: String = "Your request has expired",
        cause: Throwable = null) extends EchoedException(message, cause)
