package com.echoed.chamber.filters

import javax.servlet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.slf4j.LoggerFactory
import scala.reflect.BeanProperty
import scala.collection.JavaConversions._


class AccessControlHeadersFilter extends Filter {

    private val logger = LoggerFactory.getLogger(classOf[AccessControlHeadersFilter])

    @BeanProperty var domain: String = _

    def init(filterConfig: FilterConfig) {}

    def doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, chain: FilterChain) {
        val request = servletRequest.asInstanceOf[HttpServletRequest]
        val response = servletResponse.asInstanceOf[HttpServletResponse]

        val origin = Option(request.getHeader("Origin"))
        if (origin.map(_.endsWith(domain)).getOrElse(false)) {
            val headers =
                if (logger.isDebugEnabled) {
                    request.getHeaderNames.map(h => "\n    %s: %s" format(h, request.getHeader(h))).mkString
                } else "<empty>"
            logger.debug("Adding Access-Control-Allow headers for %s" format headers)
            response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"))
            response.addHeader("Access-Control-Allow-Methods", "POST, PUT, DELETE, GET, OPTIONS")
            response.addHeader("Access-Control-Allow-Headers", "Content-Type, X-Scheme, X-Forwarded-For")
            response.addHeader("Access-Control-Allow-Credentials", "true")
        } else {
            logger.debug("Did not add Access-Control-Allow headers: origin {} does not end with {}", origin.orNull, domain)
        }

        chain.doFilter(request, response)
    }

    def destroy() {}

}
