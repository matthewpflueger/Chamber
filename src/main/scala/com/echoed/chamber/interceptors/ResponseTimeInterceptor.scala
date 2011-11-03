package com.echoed.chamber.interceptors

//import org.apache.log4j.Logger
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.springframework.web.servlet.{ModelAndView, HandlerInterceptor}
import org.slf4j.LoggerFactory

class ResponseTimeInterceptor extends HandlerInterceptor {

    val logger = LoggerFactory.getLogger(classOf[ResponseTimeInterceptor])
    private val START = "_httpRequestStartTime";

    def preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object) = {
        request.setAttribute(START, System.currentTimeMillis());
        true;
    }

    def postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object, modelAndView: ModelAndView) {
        //
    }

    def afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Object, ex: Exception) {
        if (logger.isDebugEnabled()) {
            //also - tracking response time should actually go into a web event listener...
            val start: Option[Long] = Option(request.getAttribute(START).asInstanceOf[Long]);
            if (start != None) {
                val interval: Long = System.currentTimeMillis() - start.get;
                logger.debug("Http request/response time: {} ", interval);
            }
        }
    }
}
