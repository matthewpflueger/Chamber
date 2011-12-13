package com.echoed.util

import javax.servlet.http.{HttpServletRequest, HttpServletResponse, Cookie}


trait CookieManager {

    def createCookie(name: String, value: String): Cookie

    def addCookie(httpServletResponse: HttpServletResponse, name: String, value: String)

    def getCookie(httpServletRequest: HttpServletRequest, name: String): Option[Cookie]

    def getCookieValue(httpServletRequest: HttpServletRequest, name: String, default: String = "")

    def deleteCookie(httpServletResponse: HttpServletResponse, name:String)
}
