package com.echoed.chamber.filters

import javax.servlet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.slf4j.LoggerFactory
import scala.reflect.BeanProperty
import scala.collection.JavaConversions._


class AccessControlHeadersFilter extends Filter {

    def init(filterConfig: FilterConfig) {}

    def doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, chain: FilterChain) {
        val request = servletRequest.asInstanceOf[HttpServletRequest]
        val response = servletResponse.asInstanceOf[HttpServletResponse]

        //leaving this open to allow for our partner widgets to access us easily (no iframes)
        response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"))
        response.addHeader("Access-Control-Allow-Methods", "POST, PUT, DELETE, GET, OPTIONS")
        response.addHeader(
                "Access-Control-Allow-Headers",
                "Origin, Host, Allow, Accept, Content-Type, X-Real-IP, X-Scheme, X-Forwarded-For, Authorization")
        response.addHeader("Access-Control-Allow-Credentials", "true")

        chain.doFilter(request, response)
    }

    def destroy() {}

}
