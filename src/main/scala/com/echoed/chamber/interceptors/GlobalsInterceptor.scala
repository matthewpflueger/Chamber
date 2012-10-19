package com.echoed.chamber.interceptors


import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.springframework.web.servlet.{ModelAndView, HandlerInterceptor}
import org.slf4j.LoggerFactory
import scala.reflect.BeanProperty
import com.echoed.chamber.services.GlobalsManager
import org.springframework.web.servlet.support.RequestContextUtils
import scala.collection.JavaConversions._


class GlobalsInterceptor extends HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(classOf[GlobalsInterceptor])

    @BeanProperty var globalsManager: GlobalsManager = _

    def preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object) = {
        true;
    }

    def postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object, mv: ModelAndView) {
        if (mv != null && mv.getViewName != null && !mv.getViewName.startsWith("redirect:")) {
            if (request.getScheme == "https" || Option(request.getHeader("X-Scheme")).getOrElse("").equals("https")) {
                mv.getModel.putAll(globalsManager.globals(globalsManager.httpsUrls, RequestContextUtils.getLocale(request)))
            } else {
                mv.getModel.putAll(globalsManager.globals(globalsManager.httpUrls, RequestContextUtils.getLocale(request)))
            }
        }
    }

    def afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Object, ex: Exception) {
        //
    }
}
