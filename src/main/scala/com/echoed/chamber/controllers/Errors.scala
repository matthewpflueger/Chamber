package com.echoed.chamber.controllers

import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.EchoedException

trait Errors { this: ModelAndView =>

    def addError(defaultMessage: String, code: Option[String] = None, arguments: Option[Array[AnyRef]] = None) {
        addError(new EchoedException(
            msg = defaultMessage,
            cde = code,
            args = arguments))
    }

    def addError(e: Throwable) {
        addError(Some(e))
    }

    def addError(e: Option[Throwable]) {
        e.foreach { _ match {
            case ex: EchoedException =>
                val errors = ex.asErrors
                addObject(errors.getObjectName, errors)
            case other =>
                addError(Some(EchoedException(other.getMessage, other)))
        }}
    }

}
