package com.echoed.chamber

import dao.RetailerConfirmationDao
import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.openqa.selenium.firefox.FirefoxDriver
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import org.openqa.selenium.WebDriver


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:itest.xml"))
class EchoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers {

    @Autowired @BeanProperty var retailerConfirmationDao: RetailerConfirmationDao = null
    @Autowired @BeanProperty var webDriver: WebDriver = null

    val buttonUrl = "http://v1-api.echoed.com/echo/button"
    val buttonRedirectUrl = "http://v1-cdn.echoed.com/button_echoed.png"

    new TestContextManager(this.getClass()).prepareTestInstance(this)



    feature("A user can share their purchase by clicking on the Echo button on a retailer's purchase confirmation page") {

        info("As a recent purchaser")
        info("I want to be able to click on the Echo button")
        info("So that I can share my purchase with friends")


        scenario("unknown user clicks on button and is redirected to login page") {
            given("a request to echo a purchase")
            when("the user is unrecognized (no cookie)")
            then("redirect to the login page")
            and("record the attempt to echo in the database as a retailer confirmation")
            1 should equal (2) //force failure here
        }

    }

}