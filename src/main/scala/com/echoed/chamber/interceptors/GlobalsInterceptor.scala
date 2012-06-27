package com.echoed.chamber.interceptors


import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.springframework.web.servlet.{ModelAndView, HandlerInterceptor}
import org.slf4j.LoggerFactory
import scala.reflect.BeanProperty
import com.echoed.chamber.services.GlobalsManager
import org.springframework.web.servlet.support.RequestContextUtils

class GlobalsInterceptor extends HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(classOf[GlobalsInterceptor])

    @BeanProperty var globalsManager: GlobalsManager = _

    def preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object) = {
        true;
    }

    def postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object, mv: ModelAndView) {
        if (mv != null && mv.getViewName != null && !mv.getViewName.startsWith("redirect:")) {
            if (request.getScheme == "https" || Option(request.getHeader("X-Scheme")).getOrElse("").equals("https")) {
                globalsManager.addGlobals(mv.getModel, globalsManager.getHttpsUrls(), RequestContextUtils.getLocale(request))
            } else {
                globalsManager.addGlobals(mv.getModel, globalsManager.getHttpUrls(), RequestContextUtils.getLocale(request))
            }
        }
    }

    def afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Object, ex: Exception) {
        //
    }
}
