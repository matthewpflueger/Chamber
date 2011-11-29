package com.echoed.chamber.services


case class ErrorMessage(
        message: String,
        cause: Option[Throwable] = None) extends Throwable(
                message,
                cause.orNull) with Message {

    def this(cause: Throwable) = this(cause.getMessage, Option(cause))
}












