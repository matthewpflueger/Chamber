package com.echoed.chamber.filters

import javax.servlet._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eclipse.jetty.continuation.{Continuation, ContinuationListener, ContinuationSupport}
import org.slf4j.LoggerFactory


class JsonpCallbackFilter extends Filter {

    private val logger = LoggerFactory.getLogger(classOf[JsonpCallbackFilter])

    def init(filterConfig: FilterConfig) {}

    def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val callback = Option(request.getParameter("callback"))
        if (!callback.isDefined) {
            chain.doFilter(request, response)
            return
        }

        logger.debug("Wrapping response for jsonp request with callback {}", callback)

        val continuation = ContinuationSupport.getContinuation(request)
        var wrapper = new GenericResponseWrapper(response.asInstanceOf[HttpServletResponse])

        chain.doFilter(request, wrapper)

        //if servlet is not using continuations or using undispatch then the isResponseWrapped will return false
        //otherwise a continuation listener will be added...
        if (!continuation.isResponseWrapped) {
            finishResponse(callback.get, wrapper, response)
        } else {
            logger.debug("Adding continuation listener")
            continuation.addContinuationListener(new ContinuationListener() {
                def onComplete(continuation: Continuation) {
                    finishResponse(callback.get, wrapper, continuation.getServletResponse)
                }

                def onTimeout(continuation: Continuation) {
                    logger.error("Timeout occurred on wrapped response")
                    finishResponse(callback.get, wrapper, continuation.getServletResponse)
                }
            })
        }
    }

    def finishResponse(callback: String, wrapper: GenericResponseWrapper, response: ServletResponse) {
        val output = response.getOutputStream
        if (wrapper.getContentType == null || wrapper.getContentType.contains("json")) {
            logger.debug("Writing jsonp callback {}", callback)
            response.setContentType("application/x-javascript")
            output.write("%s(".format(callback).getBytes("UTF-8"))
            output.write(wrapper.output.toByteArray)
            output.write(");".getBytes("UTF-8"))
        } else {
            logger.warn("Callback {} found but content type is not json but {}", callback, wrapper.getContentType)
            output.write(wrapper.output.toByteArray)
        }
    }

    def destroy() {}

}
