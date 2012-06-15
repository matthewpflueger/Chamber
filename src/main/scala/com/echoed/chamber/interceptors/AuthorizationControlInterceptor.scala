package com.echoed.chamber.interceptors

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.springframework.web.servlet.{ModelAndView, HandlerInterceptor}
import org.slf4j.LoggerFactory
import scala.reflect.BeanProperty
import com.echoed.chamber.controllers.CookieManager

class AuthorizationControlInterceptor extends HandlerInterceptor {

    val logger = LoggerFactory.getLogger(classOf[AuthorizationControlInterceptor])

    @BeanProperty var cookieManager: CookieManager = _
    @BeanProperty var httpsUrl: String = _

    def preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object) = {
        val path = request.getRequestURI

        if (path == null || (!path.startsWith("/partner") && !path.startsWith("/admin"))) {
            true
        } else {
            val isPartner = path.startsWith("/partner")
            val isJson = request.getHeader("Accept").contains("application/json")
            if (!"https".equals(request.getScheme)) {
                if (isJson) {
                    response.setStatus(401)
                } else if (isPartner) {
                    response.sendRedirect("%s/partner/login" format httpsUrl)
                } else {
                    response.sendRedirect("%s/admin/login" format httpsUrl)
                }
                false
            } else if (path.startsWith("/partner/login")) {
                true
            } else if (path.startsWith("/admin/login")) {
                true
            } else {
                val pu = cookieManager.findPartnerUserCookie(request)
                val au = cookieManager.findAdminUserCookie(request)
                if (isPartner) {
                    if (pu.filter(_.length == 36).isDefined) true
                    else {
                        if (isJson) response.setStatus(401)
                        else response.sendRedirect("%s/partner/login" format httpsUrl)
                        false
                    }
                } else {
                    if (au.filter(_.length == 36).isDefined) true
                    else {
                        if (isJson) response.setStatus(401)
                        else response.sendRedirect("%s/admin/login" format httpsUrl)
                        false
                    }
                }
            }
        }
    }

    def postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object, modelAndView: ModelAndView) {
        //
    }

    def afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Object, ex: Exception) {
        //
    }
}
