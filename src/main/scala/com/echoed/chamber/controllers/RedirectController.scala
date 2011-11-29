package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod, RequestParam}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import com.echoed.chamber.services.echo.EchoService
import com.echoed.chamber.domain.EchoPossibility
import org.eclipse.jetty.continuation.ContinuationSupport
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping(Array("/redirect"))
class RedirectController {

    private final val logger = LoggerFactory.getLogger(classOf[RedirectController])

    @BeanProperty var echoService: EchoService = _

    @RequestMapping(method = Array(RequestMethod.GET))
    def redirect(@RequestParam("id") echoId: String,
                 httpServletRequest: HttpServletRequest,
                 httpServletResponse: HttpServletResponse) = {


        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        //Retrieve the Echo
        logger.debug("Retrieve Ech  o Possibility with Id {}", echoId)
        if (echoId == None || echoId == "") {
            new ModelAndView("redirect:http://www.echoed.com")
        }
        else Option(continuation.getAttribute("modelAndView")).getOrElse({

            continuation.suspend(httpServletResponse)

            val futureEchoPossibility = echoService.getEchoPossibility(echoId)
            futureEchoPossibility
                    .onResult({
                case echoPossibility: EchoPossibility =>

                    val redirectUrl: String = echoPossibility.getImageUrl() //TODO REPLACE IMAGE URL WITH ACTUAL REDIRECT URL
                    //TODO RECORD CLICK

                    logger.debug("Sending User to Url: {}", redirectUrl)
                    val modelAndView: ModelAndView = new ModelAndView("redirect:" + redirectUrl)
                    continuation.setAttribute("modelAndView", modelAndView)
                    continuation.resume()
            })
                    .onException({
                case e =>
                    val modelAndView: ModelAndView = new ModelAndView("redirect:http://www.echoed.com") //TODO REPLACE WITH EXCEPTION and ERROR PAGE
                    continuation.setAttribute("modelAndView", modelAndView)
                    continuation.resume()
            })
                    .onTimeout({
                case e =>
                    val modelAndView: ModelAndView = new ModelAndView("redirect:http://www.echoed.com") //TODO REPLACE WITH EXCEPTION and ERROR PAGE
                    continuation.setAttribute("modelAndView", modelAndView)
                    continuation.resume()
            })
            continuation.undispatch()
        })
    }
}
