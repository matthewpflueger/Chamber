package com.echoed.chamber.services

import akka.AkkaException
import org.springframework.validation.Errors


case class EchoedException(msg: String = "", cse: Throwable = null, errors: Option[Errors] = None) extends AkkaException(msg, cse)














