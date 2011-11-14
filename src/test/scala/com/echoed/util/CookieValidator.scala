package com.echoed.util

import org.scalatest.matchers.ShouldMatchers
import org.openqa.selenium.WebDriver


object CookieValidator extends ShouldMatchers {

    def validate(webDriver: WebDriver, cookieName: String, cookieValue: String) {
        val cookie = Option(webDriver.manage().getCookieNamed(cookieName))
        cookie should not be (None)
        cookie.get.getValue should be (cookieValue)
    }

    def validateNoValue(webDriver: WebDriver, cookieName: String) {
        validate(webDriver, cookieName, "")
    }

    def validateNoCookie(webDriver: WebDriver, cookieName: String) {
        val cookie = Option(webDriver.manage().getCookieNamed(cookieName))
        cookie should be (None)
    }
}
