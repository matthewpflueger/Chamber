package com.echoed.chamber.interceptors

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.springframework.web.servlet.{ModelAndView, HandlerInterceptor}
import org.slf4j.LoggerFactory
import scala.reflect.BeanProperty

class AccessControlInterceptor extends HandlerInterceptor {

    val logger = LoggerFactory.getLogger(classOf[AccessControlInterceptor])

    @BeanProperty var domain: String = _

    def preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object) = {
        if (Option(request.getHeader("Origin")).map(_.endsWith(domain)).getOrElse(false)) {
            response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"))
            response.addHeader("Access-Control-Allow-Methods", "*") //POST, PUT, DELETE, GET, OPTIONS")
            response.addHeader("Access-Control-Allow-Headers", "*")
            response.addHeader("Access-Control-Allow-Credentials", "true")
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
