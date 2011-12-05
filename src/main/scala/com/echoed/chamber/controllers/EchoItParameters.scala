package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import org.eclipse.jetty.continuation.ContinuationSupport
import com.echoed.chamber.services.echoeduser.EchoedUserServiceLocator
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.echoed.util.CookieManager
import akka.dispatch.Future
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.bind.annotation._
import com.echoed.chamber.domain.{EchoClick, EchoPossibility}
import com.echoed.chamber.services.echo.{EchoResponseMessage, EchoRequestMessage, EchoService}


case class EchoItParameters(
            @BeanProperty var facebookMessage: String = null,
            @BeanProperty var postToFacebook: Boolean = false,
            @BeanProperty var twitterMessage: String = null,
            @BeanProperty var postToTwitter: Boolean = false,
            @BeanProperty var echoedUserId: String = null,
            @BeanProperty var echoPossibility: String = null) {

        def this() = this(
            null,
            false,
            null,
            false,
            null,
            null)

        def createEchoRequestMessage = new EchoRequestMessage(
                echoedUserId,
                echoPossibility,
                Option(facebookMessage),
                postToFacebook,
                Option(twitterMessage),
                postToTwitter)
}


