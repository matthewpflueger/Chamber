package com.echoed.chamber.interceptors

//import org.apache.log4j.Logger

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.springframework.web.servlet.{ModelAndView, HandlerInterceptor}
import org.slf4j.LoggerFactory
import org.eclipse.jetty.continuation.ContinuationSupport
import scala.reflect.BeanProperty

class AccessControlInterceptor extends HandlerInterceptor {

    val logger = LoggerFactory.getLogger(classOf[AccessControlInterceptor])

    @BeanProperty var domain: String = _

    def preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object) = {
        val continuation = ContinuationSupport.getContinuation(request)
        if (continuation.isResumed && Option(request.getHeader("Origin")).map(_.endsWith(domain)).getOrElse(false)) {
            response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"))
            response.addHeader("Access-Control-Allow-Methods", request.getMethod)
            response.addHeader("Access-Control-Allow-Credentials", "true")
        }
        true;
    }

    def postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object, modelAndView: ModelAndView) {
        //
    }

    def afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Object, ex: Exception) {
        //
    }
}
