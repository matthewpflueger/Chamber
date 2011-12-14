package com.echoed.chamber.services

import akka.AkkaException


case class EchoedException(msg: String = "", cse: Throwable = null) extends AkkaException(msg, cse)














