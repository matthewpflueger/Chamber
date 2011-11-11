package com.echoed.chamber

import dao.EchoedUserDao
import domain.EchoedUser
import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.openqa.selenium.Cookie
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import org.openqa.selenium.WebDriver
import java.util.Properties
import java.util.Date
import tags.IntegrationTest



@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:itest.xml"))
class EchoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers {

    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = null
    @Autowired @BeanProperty var echoHelper: EchoHelper = null
    @Autowired @BeanProperty var webDriver: WebDriver = null

    @Autowired @BeanProperty var urls: Properties = null

    new TestContextManager(this.getClass()).prepareTestInstance(this)


    var echoUrl: String = null
    var loginViewUrl: String = null
    var confirmViewUrl: String = null

    {
        echoUrl = urls.getProperty("echoUrl")
        loginViewUrl = urls.getProperty("loginViewUrl")
        confirmViewUrl = urls.getProperty("confirmViewUrl")
        echoUrl != null && loginViewUrl != null && confirmViewUrl != null
    } ensuring (_ == true, "Missing parameters")


    feature("A user can share their purchase by clicking on the Echo button on a retailer's purchase confirmation page") {

        info("As a recent purchaser")
        info("I want to be able to click on the Echo button")
        info("So that I can share my purchase with friends")

        scenario("unknown user clicks on echo button with invalid parameters and is redirected to Echoed's info page", IntegrationTest) {
            given("a request to echo a purchase")
            when("the user is unrecognized (no cookie) and invalid parameters")
            then("redirect to Echoed's info page")
            pending
        }

        scenario("when a known user clicks on echo button with invalid parameters and is redirected to closet", IntegrationTest) {
            given("a request to echo a purchase")
            when("the user is recognized (has cookie) and invalid parameters")
            then("redirect to the user's Echoed closet")
            and("ask user to report the error (so we may contact the retailer)")
            pending
        }

        scenario("unknown user clicks on echo button with valid parameters and is redirected to login page", IntegrationTest) {
            given("a request to echo a purchase")
            when("the user is unrecognized (no cookie) and with valid parameters")
            val (echoPossibility, count) = echoHelper.setupEchoPossibility(step = "login") //this must match proper step...
            webDriver.manage().deleteCookieNamed("echoedUserId")
            webDriver.navigate.to(echoUrl + echoPossibility.generateUrlParameters)

            then("redirect to the login page")
            webDriver.getCurrentUrl should equal (loginViewUrl)

            and("record the EchoPossibility in the database")
            echoHelper.validateEchoPossibility(echoPossibility, count)
        }

        scenario("a known user clicks on echo button with valid parameters and is redirected to confirmation page", IntegrationTest) {
            val echoedUser = new EchoedUser(null, "matthew.pflueger", "matthew.pflueger@gmail.com", "Matthew", "Pflueger", null, null)
            echoedUserDao.insertOrUpdate(echoedUser)
            val (echoPossibility, count) = echoHelper.setupEchoPossibility(step = "confirm", echoedUserId = echoedUser.id)

            given("a request to echo a purchase")
            when("the user is recognized (has a cookie) and with valid parameters")
            val cookie = new Cookie.Builder("echoedUserId", echoedUser.id)
                    .domain(".echoed.com")
                    .expiresOn(new Date((new Date().getTime + (1000*60*60*24))))
                    .build()
            webDriver.manage().addCookie(cookie)
            webDriver.navigate().to(echoUrl + echoPossibility.generateUrlParameters)

            then("redirect to the echo confirmation page")
            webDriver.getCurrentUrl should equal (confirmViewUrl)

            and("record the EchoPossibility in the database")
            echoHelper.validateEchoPossibility(echoPossibility, count)
        }

    }

}