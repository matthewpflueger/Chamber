package com.echoed.chamber.interceptors

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.springframework.web.servlet.{ModelAndView, HandlerInterceptor}
import org.slf4j.LoggerFactory
import scala.reflect.BeanProperty
import com.echoed.chamber.controllers.CookieManager
import java.util.UUID

class BrowserIdInterceptor extends HandlerInterceptor {

    val logger = LoggerFactory.getLogger(classOf[BrowserIdInterceptor])

    @BeanProperty var cookieManager: CookieManager = _

    def preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object) = {
        cookieManager.findBrowserIdCookie(request).orElse {
            Option(cookieManager.addBrowserIdCookie(response, UUID.randomUUID().toString, request))
        }
        true;
    }

    def postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object, mv: ModelAndView) {
        //
    }

    def afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Object, ex: Exception) {
        //
    }
}
