package com.echoed.chamber.filters

import javax.servlet._
import javax.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory


class JsonpCallbackFilter extends Filter {

    private val log = LoggerFactory.getLogger(classOf[JsonpCallbackFilter])

    def init(filterConfig: FilterConfig) {}

    def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val cb = Option(request.getParameter("callback"))
        if (!cb.isDefined) {
            chain.doFilter(request, response)
            return
        }
        val callback = cb.get

        log.debug("Wrapping response for jsonp request with callback {}", callback)

        val wrapper = new GenericResponseWrapper(response.asInstanceOf[HttpServletResponse])

        chain.doFilter(request, wrapper)

        if (!request.isAsyncStarted) finishResponse(callback, wrapper, response)
        else addAsyncListener(callback, wrapper, request, response)
    }

    def finishResponse(callback: String, wrapper: GenericResponseWrapper, response: ServletResponse) {
        val output = response.getOutputStream
        if (wrapper.getContentType == null || wrapper.getContentType.contains("json")) {
            log.debug("Writing jsonp callback {}", callback)
            response.setContentType("application/x-javascript")
            output.write("%s(".format(callback).getBytes("UTF-8"))
            output.write(wrapper.output.toByteArray)
            output.write(");".getBytes("UTF-8"))
        } else {
            log.debug("Callback {} found but content type is not json but {}", callback, wrapper.getContentType)
            output.write(wrapper.output.toByteArray)
        }
    }

    def addAsyncListener(
            callback: String,
            wrapper: GenericResponseWrapper,
            request: ServletRequest,
            response: ServletResponse) {

        log.debug("Adding async listener for callback {}", callback)
        try {
            request.getAsyncContext.addListener(new AsyncListener() {
                def onComplete(event: AsyncEvent) {
                    finishResponse(callback, wrapper, response)
                }

                def onTimeout(event: AsyncEvent) {
                    log.debug("Timeout occurred on wrapped response for callback {}", callback)
                    finishResponse(callback, wrapper, response)
                }

                def onError(event: AsyncEvent) {
                    log.error("Received error on wrapped response for callback %s" format(callback), event.getThrowable)
                }

                def onStartAsync(event: AsyncEvent) {}
            })
        } catch {
            case e: IllegalStateException =>
                log.debug("Caught IllegalStateException fetching async context for callback {}: {}", callback, e.getMessage)
        }
    }

    def destroy() {}
}
