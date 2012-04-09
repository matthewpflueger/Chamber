package com.echoed.chamber.controllers

import javax.servlet.http.{Cookie, HttpServletResponse, HttpServletRequest}
import scala.reflect.BeanProperty
import org.slf4j.LoggerFactory
import com.echoed.chamber.domain.{RetailerUser, EchoClick, EchoedUser,AdminUser}
import com.echoed.util.CookieToString


class CookieManager {

    private val logger = LoggerFactory.getLogger(classOf[CookieManager])

    @BeanProperty var domain = ".echoed.com"
    @BeanProperty var path = "/"
    @BeanProperty var sessionAge = 30*60 //30 minutes
    @BeanProperty var persistentAge = 31556926 //1 year
    @BeanProperty var httpOnly = true

    @BeanProperty var echoedUserCookieName = "eu"
    @BeanProperty var echoClickCookieName = "ec"
    @BeanProperty var partnerUserCookieName = "pu"
    @BeanProperty var adminUserCookieName = "au"

    private var baseDomain: String = "echoed.com"

    def init() {
        baseDomain = if (domain.charAt(0) == '.') domain.substring(1) else domain
    }

    def addEchoedUserCookie(
            response: HttpServletResponse = null,
            echoedUser: EchoedUser = null,
            request: HttpServletRequest = null) = {
        val cookie = makeCookie(echoedUserCookieName, Option(echoedUser).map(_.id), Option(request), Option(persistentAge))
        Option(response).foreach(_.addCookie(cookie))
        cookie
    }

    def findEchoedUserCookie(request: HttpServletRequest) = {
        getCookie(request, echoedUserCookieName).flatMap(c => Option(c.getValue))
    }

    def addEchoClickCookie(
            response: HttpServletResponse = null,
            echoClick: EchoClick = null,
            request: HttpServletRequest = null) = {
        val cookie = makeCookie(echoClickCookieName, Option(echoClick).map(_.id), Option(request), Option(persistentAge))
        Option(response).foreach(_.addCookie(cookie))
        cookie
    }

    def findEchoClickCookie(request: HttpServletRequest) = {
        getCookie(request, echoClickCookieName).flatMap(c => Option(c.getValue))
    }

    def addAdminUserCookie(
        response: HttpServletResponse = null,
        adminUser: AdminUser = null,
        request: HttpServletRequest= null) = {
        
        val cookie = makeCookie(adminUserCookieName, Option(adminUser).map(_.id), Option(request), Option(sessionAge), true)
        Option(response).foreach(_.addCookie(cookie))
        cookie
        
    }
    
    def findAdminUserCookie(request: HttpServletRequest) = {
        getCookie(request, adminUserCookieName).flatMap(c => Option(c.getValue))
    }
    
    def addPartnerUserCookie(
            response: HttpServletResponse = null,
            partnerUser: RetailerUser = null,
            request: HttpServletRequest = null) = {
        val cookie = makeCookie(partnerUserCookieName, Option(partnerUser).map(_.id), Option(request), Option(sessionAge), true)
        Option(response).foreach(_.addCookie(cookie))
        cookie
    }

    def findPartnerUserCookie(request: HttpServletRequest) = {
        getCookie(request, partnerUserCookieName).flatMap(c => Option(c.getValue))
    }

    def getCookie(httpServletRequest: HttpServletRequest, name: String) = {
        val cookie = httpServletRequest.getCookies()
            .find(cookie => if (cookie.getName == name) true else false)
            .orElse(Some(new Cookie(name, null)))
        logger.debug("Found cookie {}={}", cookie.get.getName, cookie.get.getValue)
        cookie
    }

    def determineDomain(request: Option[HttpServletRequest] = None) = {
        request.map { r =>
            val serverName = r.getServerName
            if (serverName.endsWith(baseDomain)) domain else serverName
        }.getOrElse(domain)
    }

    def makeCookie(
            name: String,
            value: Option[String] = None,
            request: Option[HttpServletRequest] = None,
            expiresIn: Option[Int] = None,
            secure: Boolean = false) = {
        val cookie = new Cookie(name, value.getOrElse("")) with CookieToString
        cookie.setDomain(determineDomain(request))
        cookie.setPath(path)
        cookie.setHttpOnly(httpOnly)
        cookie.setSecure(secure)
        if (value == None) cookie.setMaxAge(0) //delete the cookie
        else if (expiresIn != None) cookie.setMaxAge(expiresIn.get) //not a session cookie, set the max age...
        logger.debug("Created new {}", cookie)
        cookie
    }

}
