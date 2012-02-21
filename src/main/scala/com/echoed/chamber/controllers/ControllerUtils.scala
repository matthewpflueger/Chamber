package com.echoed.chamber.controllers

import org.springframework.web.servlet.ModelAndView
import org.slf4j.LoggerFactory
import org.eclipse.jetty.continuation.Continuation


object ControllerUtils {

    private val logger = LoggerFactory.getLogger(classOf[ControllerUtils])

    def error(view: String, e: Option[Throwable] = None)(implicit continuation: Continuation) = {
        e.foreach(ex => logger.error("%s" format ex.getMessage, ex))
        val modelAndView = new ModelAndView(view) with Errors
        modelAndView.addError(e)
        continuation.setAttribute("modelAndView", modelAndView)
        if (continuation.isSuspended) continuation.resume
        modelAndView
    }
}

class ControllerUtils {

}
