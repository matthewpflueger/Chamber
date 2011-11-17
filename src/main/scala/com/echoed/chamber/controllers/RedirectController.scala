package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{CookieValue, RequestMapping, RequestMethod,RequestParam}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import reflect.BeanProperty
import org.slf4j.LoggerFactory


@Controller
@RequestMapping(Array("/redirect"))
class RedirectController {

  private final val logger = LoggerFactory.getLogger(classOf[RedirectController])

  @RequestMapping(method=Array(RequestMethod.GET))
  def redirect(@RequestParam("echo") echoId:String,
               httpServletRequest: HttpServletRequest,
               httpServletResponse: HttpServletResponse){
    httpServletResponse.sendRedirect("http://www.twitter.com")
  }
}