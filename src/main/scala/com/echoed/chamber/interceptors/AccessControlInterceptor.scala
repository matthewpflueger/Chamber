package com.echoed.chamber.interceptors

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.springframework.web.servlet.{ModelAndView, HandlerInterceptor}
import org.slf4j.LoggerFactory
import scala.reflect.BeanProperty
import scala.collection.JavaConversions._

class AccessControlInterceptor extends HandlerInterceptor {

    val logger = LoggerFactory.getLogger(classOf[AccessControlInterceptor])

    @BeanProperty var domain: String = _

    def preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object) = {
        val origin = Option(request.getHeader("Origin"))
        if (origin.map(_.endsWith(domain)).getOrElse(false)) {
            val headers =
                if (logger.isDebugEnabled) {
                    request.getHeaderNames.map(h => "    %s: %s\n" format(h, request.getHeader(h)))
                } else ""
            logger.debug("Adding Access-Control-Allow headers for\n{}", headers)
            response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"))
            response.addHeader("Access-Control-Allow-Methods", "*") //POST, PUT, DELETE, GET, OPTIONS")
            response.addHeader("Access-Control-Allow-Headers", "*")
            response.addHeader("Access-Control-Allow-Credentials", "true")
        } else {
            logger.debug("Did not add Access-Control-Allow headers: origin {} does not end with {}", origin.orNull, domain)
        }
        true
    }

    def postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object, modelAndView: ModelAndView) {
        //
    }

    def afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Object, ex: Exception) {
        //
    }
}
