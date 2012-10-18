package com.echoed.chamber.controllers

import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.EchoedException

trait Errors { this: ModelAndView =>

    def addError(e: Throwable, errorsForObjectName: Option[String] = None, attributeName: Option[String] = None) {
        e match {
            case ex: EchoedException =>
                val errors = ex.asErrors(errorsForObjectName)
                addObject(attributeName.getOrElse(ex.getClass.getName), errors)
            case other =>
                addError(new EchoedException(other.getMessage, other), errorsForObjectName, attributeName)
        }
    }

}
