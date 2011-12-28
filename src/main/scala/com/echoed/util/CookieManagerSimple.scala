package com.echoed.util

import reflect.BeanProperty
import org.slf4j.LoggerFactory
import javax.servlet.http.{Cookie, HttpServletRequest, HttpServletResponse}


class CookieManagerSimple extends CookieManager {

    private final val logger = LoggerFactory.getLogger(classOf[CookieManagerSimple])

    @BeanProperty var domain: String = ".echoed.com"
    @BeanProperty var path: String = "/"
    @BeanProperty var expiresIn: Int = -1


    def createCookie(name: String, value: String) = {
        val cookie = new Cookie(name, value)
        cookie.setDomain(domain)
        cookie.setPath("/")
        cookie.setMaxAge(if (Option(value) == None) 0 else expiresIn)
        cookie
    }

    def addCookie(httpServletResponse: HttpServletResponse, name: String, value: String) {
        val cookie = createCookie(name, value)
        logger.debug("Adding cookie {} = {}", cookie.getName, cookie.getValue)
        httpServletResponse.addCookie(cookie)
    }

    def deleteCookie(httpServletResponse: HttpServletResponse, name:String) {
        val cookie = new Cookie(name,"")
        cookie.setDomain(domain)
        cookie.setMaxAge(0)
        cookie.setPath( "/" );
        httpServletResponse.addCookie(cookie)
    }

    def getCookie(httpServletRequest: HttpServletRequest, name: String) = {
        httpServletRequest.getCookies().find(cookie => if (cookie.getName == name) true else false)
    }

    def getCookieValue(httpServletRequest: HttpServletRequest, name: String, default: String = "") = {
        getCookie(httpServletRequest, name).getOrElse(new Cookie(name, default)).getValue
    }
}
