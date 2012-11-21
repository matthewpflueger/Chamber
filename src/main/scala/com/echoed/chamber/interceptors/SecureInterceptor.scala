package com.echoed.chamber.interceptors

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.springframework.web.servlet.{ModelAndView, HandlerInterceptor}
import org.slf4j.LoggerFactory
import scala.reflect.BeanProperty
import org.springframework.web.method.HandlerMethod
import com.echoed.chamber.controllers.interceptors.Secure
import java.net.URLEncoder

class SecureInterceptor extends HandlerInterceptor {

    val logger = LoggerFactory.getLogger(classOf[SecureInterceptor])

    @BeanProperty var httpsUrl: String = _

    def preHandle(request: HttpServletRequest, response: HttpServletResponse, h: Object) = {
        val handler = h.asInstanceOf[HandlerMethod]
        val optSecure = Option(handler.getMethodAnnotation(classOf[Secure]))
                        .orElse(Option(handler.getBean.getClass.getAnnotation(classOf[Secure])))
        val isHttps = request.isSecure || Option(request.getHeader("X-Scheme")).filter(_.equals("https")).isDefined
        val isGet = request.getMethod == "GET"

        if (optSecure.isEmpty || isHttps) {
            request.setAttribute("isSecure", isHttps)
            true
        } else {
            val secure = optSecure.get
            val path = request.getRequestURI
            logger.debug("Invalid access to secure resource {}", path)

            if (!isGet || !secure.redirect) response.sendError(401, "Insecure access to secure resource")
            else {
                val originalUrl = path + Option(request.getQueryString).map("?" + _).getOrElse("")
                val originalUrlParam =
                    if (secure.addRedirectParam) {
                        URLEncoder.encode("%s=%s" format(secure.redirectParam, originalUrl), "UTF-8")
                    } else ""

                val redirectUrl = Option(secure.redirectToPath)
                    .flatMap { path => if (path.length > 0) Some(path) else None }
                    .map { path => "%s%s?%s" format(httpsUrl, path, originalUrlParam) }
                    .orElse(Some(httpsUrl + originalUrl)).get

                response.sendRedirect(redirectUrl)
                logger.debug("Redirected to {}", redirectUrl)
            }

            false
        }
    }

    def postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object, modelAndView: ModelAndView) {
        //
    }

    def afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Object, ex: Exception) {
        //
    }
}
