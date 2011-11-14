package com.echoed.util

import javax.servlet.http.{HttpServletResponse, Cookie}


trait CookieManager {

    def createCookie(name: String, value: String): Cookie

    def addCookie(httpServletResponse: HttpServletResponse, name: String, value: String)
}