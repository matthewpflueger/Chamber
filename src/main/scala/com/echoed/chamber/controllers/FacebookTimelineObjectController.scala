package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.springframework.web.bind.annotation._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.eclipse.jetty.continuation.ContinuationSupport


@Controller
@RequestMapping(Array("/fbtimeline"))
class FacebookTimelineObjectController {

    @BeanProperty var fbTimelineView: String = _

    @RequestMapping(method = Array(RequestMethod.GET))
    def showTimelineObject(
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse) = {
        
        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        
        if(continuation.isExpired){
            
        } else Option(continuation.getAttribute("modelAndView")).getOrElse {
            continuation.suspend(httpServletResponse)
            continuation.setAttribute("modelAndView","")
            continuation.undispatch()
        }
    }
}
