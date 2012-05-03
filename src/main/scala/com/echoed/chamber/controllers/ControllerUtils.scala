package com.echoed.chamber.controllers

import org.springframework.web.servlet.ModelAndView
import org.slf4j.LoggerFactory
import org.eclipse.jetty.continuation.Continuation


object ControllerUtils {

    private val logger = LoggerFactory.getLogger(classOf[ControllerUtils])

    def error(e: Throwable)(implicit continuation: Continuation): ModelAndView = error("error", Some(e))(continuation)

    def error(view: String, e: Throwable)(implicit continuation: Continuation): ModelAndView =
            error(view, Some(e))(continuation)

    def error(view: String, e: Option[Throwable] = None)(implicit continuation: Continuation): ModelAndView = {
        e.foreach(ex => logger.error("%s" format ex.getMessage, ex))
        val modelAndView = new ModelAndView(view) with Errors
        modelAndView.addError(e)
        Option(continuation).foreach { c =>
            c.setAttribute("modelAndView", modelAndView)
            if (c.isSuspended) c.resume
        }
        modelAndView
    }
}

class ControllerUtils {

}
