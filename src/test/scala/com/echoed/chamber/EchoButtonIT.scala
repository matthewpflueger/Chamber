package com.echoed.chamber

import dao.{RetailerDao, EchoPossibilityDao}
import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import org.openqa.selenium.WebDriver
import java.util.Properties


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:itest.xml"))
class EchoButtonIT extends FeatureSpec with GivenWhenThen with ShouldMatchers {

    @Autowired @BeanProperty var echoHelper: EchoHelper = null
    @Autowired @BeanProperty var webDriver: WebDriver = null

    @Autowired @BeanProperty var urls: Properties = null

    new TestContextManager(this.getClass()).prepareTestInstance(this)


    var buttonUrl: String = null
    var buttonViewUrl: String = null

    {
        buttonUrl = urls.getProperty("buttonUrl")
        buttonViewUrl = urls.getProperty("buttonViewUrl")
        buttonUrl != null && buttonViewUrl != null
    } ensuring (_ == true, "Missing parameters")




    feature("An Echo button is shown on a retailer's purchase confirmation page") {

        info("As a retailer")
        info("I want to be able to show the Echo button on my confirmation pages")
        info("So that my customers can share their purchases with friends")


        scenario("button is requested with no retailer, customer, or purchase info") {
            val count = echoHelper.getEchoPossibilityCount

            given("a request for the button")
            webDriver.navigate.to(buttonUrl)


            when("there is no other information")
            then("redirect to the button")
            webDriver.getCurrentUrl should equal (buttonViewUrl)

            and("no info should be recorded in the database")
            echoHelper.validateCountIs(count)
        }

        scenario("button is requested with invalid retailer id") {
            val count = echoHelper.getEchoPossibilityCount

            given("a request for the button")
            webDriver.navigate.to(buttonUrl + "?retailerId=foo")

            when("there is an invalid retailer id")
            then("redirect to the button")
            webDriver.getCurrentUrl should equal (buttonViewUrl)

            and("no info should be recorded in the database")
            echoHelper.validateCountIs(count)
        }

        scenario("button is requested from an unknown site") {
            given("a request for the button")
            when("the referrer is an unknown site")
            then("redirect to the button")
            and("no info should be recorded in the database")
            pending
        }

        scenario("button is requested with valid parameters") {
            val (echoPossibility, count) = echoHelper.setupEchoPossibility()

            given("a request for the button")
            webDriver.navigate.to(buttonUrl + echoPossibility.generateUrlParameters)

            when("there are valid parameters")
            then("redirect to the button")
            webDriver.getCurrentUrl should equal (buttonViewUrl)

            and("record the EchoPossibility in the database")
            echoHelper.validateEchoPossibility(echoPossibility, count)
        }

    }

}

