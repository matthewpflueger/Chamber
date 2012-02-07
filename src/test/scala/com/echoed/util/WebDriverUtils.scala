package com.echoed.util

import org.openqa.selenium.{WebDriver, Cookie}
import com.echoed.chamber.domain.EchoedUser
import org.scalatest.matchers.ShouldMatchers
import java.util.concurrent.TimeUnit
import scala.reflect.BeanProperty
import com.echoed.chamber.controllers.CookieManager
import org.slf4j.LoggerFactory
import java.util.{Calendar, Date}


class WebDriverUtils extends ShouldMatchers {

    private val logger = LoggerFactory.getLogger(classOf[WebDriverUtils])

    @BeanProperty var webDriver: WebDriver = _

    @BeanProperty var cookieManager: CookieManager = _
    @BeanProperty var domain: String = _
    @BeanProperty var echoedUrl: String = _ //"http://www.echoed.com"
    @BeanProperty var logoutUrl: String = _ //"http://www.echoed.com/logout"
    @BeanProperty var closetUrl: String = _ //"http://www.echoed.com"

    val twitterUrl = "http://www.twitter.com"
    val facebookUrl = "http://www.facebook.com"

    def init() {
        if (domain != "localhost" && domain != "localhost.local" && domain.charAt(0) != '.') {
            domain = "." + domain
        }
    }

    def findPartnerUserCookie() = {
        webDriver.manage().getCookieNamed(cookieManager.partnerUserCookieName)
    }

    def findEchoedUserCookie() = {
        webDriver.manage().getCookieNamed(cookieManager.echoedUserCookieName)
    }

    def addEchoedUserCookie(echoedUser: EchoedUser) = {
        val c = cookieManager.addEchoedUserCookie(echoedUser = echoedUser)
        val cookie = new Cookie.Builder(c.getName, c.getValue)
                .domain(domain)
                .path(c.getPath)
                .expiresOn({
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.SECOND, c.getMaxAge())
                    cal.getTime})
                .build()
        webDriver.get(echoedUrl)
        val cookies = webDriver.manage().getCookies
        logger.debug("Cookies for {} are {}", echoedUrl, cookies)
        webDriver.manage().addCookie(cookie)
        logger.debug("Set cookie {}", cookie)
        cookie
    }

    def navigateToCloset(echoedUser: EchoedUser) = {
        addEchoedUserCookie(echoedUser)

        webDriver.get(closetUrl)
        webDriver.getTitle should startWith ("Echoed")

        val pageSource = webDriver.getPageSource
        pageSource should include(echoedUser.name)
        pageSource
    }

    def clearEchoedCookies(flushCaches: Boolean = false) {
        val logout = if (flushCaches) logoutUrl + "?flush=2390uvqq03rJN_asdfoasdifu190" else logoutUrl
        webDriver.get(logout)
        Thread.sleep(1000)
        webDriver.getCurrentUrl should startWith (echoedUrl)
        webDriver.manage.deleteAllCookies()
        webDriver.get(logout)
        Thread.sleep(1000)
    }

    def clearTwitterCookies() {
        webDriver.get(twitterUrl)
        Thread.sleep(1000)
        webDriver.manage.deleteAllCookies()
        webDriver.get(twitterUrl)
        Thread.sleep(1000)
    }

    def clearFacebookCookies() {
        webDriver.get(facebookUrl)
        webDriver.manage.deleteAllCookies()
    }

}
