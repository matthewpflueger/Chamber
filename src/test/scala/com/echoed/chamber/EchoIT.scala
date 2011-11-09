package com.echoed.chamber

import dao.EchoPossibilityDao
import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.openqa.selenium.firefox.FirefoxDriver
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import org.openqa.selenium.WebDriver
import java.util.Properties


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:itest.xml"))
class EchoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers {

    @Autowired @BeanProperty var echoPossibilityDao: EchoPossibilityDao = null
    @Autowired @BeanProperty var webDriver: WebDriver = null

    @Autowired @BeanProperty var urls: Properties = null

    new TestContextManager(this.getClass()).prepareTestInstance(this)


    var echoUrl: String = null
    var loginViewUrl: String = null

    {
        echoUrl = urls.getProperty("echoUrl")
        loginViewUrl = urls.getProperty("loginViewUrl")
        echoUrl != null && loginViewUrl != null
    } ensuring (_ == true, "Missing parameters")


    feature("A user can share their purchase by clicking on the Echo button on a retailer's purchase confirmation page") {

        info("As a recent purchaser")
        info("I want to be able to click on the Echo button")
        info("So that I can share my purchase with friends")


        scenario("unknown user clicks on echo button and is redirected to login page") {
            given("a request to echo a purchase")
            webDriver.manage().deleteCookieNamed("echoedinc")
            webDriver.navigate.to(echoUrl)

            when("the user is unrecognized (no cookie)")

            then("redirect to the login page")
            and("record the attempt to echo in the database as a retailer confirmation")
            1 should equal (2) //force failure here
        }

    }

}