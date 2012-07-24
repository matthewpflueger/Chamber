package com.echoed.chamber.filters

import javax.servlet._
import javax.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory


class CacheControlFilter extends Filter {

    def init(filterConfig: FilterConfig) {}

    def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpResponse = response.asInstanceOf[HttpServletResponse]

        //anything that needs to be cached should be served from Nginx or the CDN...
        httpResponse.addHeader("Cache-Control", "no-cache")
        httpResponse.addHeader("Expires", "0")
        httpResponse.addHeader("Pragma", "no-cache")

        chain.doFilter(request, httpResponse)
    }

    def destroy() {}

}
