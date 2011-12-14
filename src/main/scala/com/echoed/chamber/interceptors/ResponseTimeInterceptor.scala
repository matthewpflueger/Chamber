package com.echoed.chamber.interceptors

//import org.apache.log4j.Logger
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.springframework.web.servlet.{ModelAndView, HandlerInterceptor}
import org.slf4j.LoggerFactory
import org.eclipse.jetty.continuation.ContinuationSupport

class ResponseTimeInterceptor extends HandlerInterceptor {

    val logger = LoggerFactory.getLogger(classOf[ResponseTimeInterceptor])
    private val START = "_httpRequestStartTime";

    def preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object) = {
        if (logger.isDebugEnabled) {
            val continuation = ContinuationSupport.getContinuation(request)
            if (continuation.isInitial) continuation.setAttribute(START, System.currentTimeMillis());
        }
        true;
    }

    def postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object, modelAndView: ModelAndView) {
        //
    }

    def afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Object, ex: Exception) {
        if (logger.isDebugEnabled()) {
            val continuation = ContinuationSupport.getContinuation(request)

            if (continuation.isResumed) {
                Option(continuation.getAttribute(START)).foreach { start =>
                    logger.debug("Http request/response time: {} ", System.currentTimeMillis - start.asInstanceOf[Long]);
                }
            }
        }
    }
}
