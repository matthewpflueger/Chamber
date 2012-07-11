package com.echoed.chamber.filters

import javax.servlet._
import javax.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory


class CacheControlFilter extends Filter {

    private val logger = LoggerFactory.getLogger(classOf[CacheControlFilter])

    def init(filterConfig: FilterConfig) {}

    def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpResponse = response.asInstanceOf[HttpServletResponse]
        httpResponse.addHeader("Cache-Control", "no-cache")
        httpResponse.addHeader("Expires", "0")
        httpResponse.addHeader("Pragma", "no-cache")

        chain.doFilter(request, httpResponse)
    }

    def destroy() {}

}
