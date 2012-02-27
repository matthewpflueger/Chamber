package com.echoed.chamber.filters

import java.io.ByteArrayOutputStream
import java.io.PrintWriter


import javax.servlet.http.{HttpServletResponse, HttpServletResponseWrapper}

class GenericResponseWrapper(response: HttpServletResponse) extends HttpServletResponseWrapper(response) {

    val output = new ByteArrayOutputStream()

    override def getOutputStream = new FilterOutputStream(output)

    override def getWriter = new PrintWriter(getOutputStream(), true)

}
