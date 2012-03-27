package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._
import java.net.URLEncoder
import org.springframework.web.servlet.ModelAndView
import scalaz._
import Scalaz._
import com.echoed.chamber.services.echo.{RecordEchoClickResponse, EchoExists, RecordEchoPossibilityResponse, EchoService}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.eclipse.jetty.continuation.{Continuation, ContinuationSupport}
import com.echoed.chamber.controllers.ControllerUtils._
import akka.dispatch.{AlreadyCompletedFuture, Future}
import com.echoed.chamber.services.echoeduser._
import com.echoed.chamber.services.partner._
import com.echoed.chamber.domain.{EchoedUser, EchoClick}
import java.util.{Map => JMap}
import com.echoed.chamber.services.EchoedException


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
