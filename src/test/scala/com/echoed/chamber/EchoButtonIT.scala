package com.echoed.chamber

import dao.RetailerConfirmationDao
import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import org.openqa.selenium.WebDriver


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:itest.xml"))
class EchoButtonIT extends FeatureSpec with GivenWhenThen with ShouldMatchers {

    @Autowired @BeanProperty var retailerConfirmationDao: RetailerConfirmationDao = null
    @Autowired @BeanProperty var webDriver: WebDriver = null

    val buttonUrl = "http://v1-api.echoed.com/echo/button"
    val buttonRedirectUrl = "http://v1-cdn.echoed.com/button_echoed.png"

    new TestContextManager(this.getClass()).prepareTestInstance(this)





    feature("An Echo button is shown on a retailer's purchase confirmation page") {

        info("As a retailer")
        info("I want to be able to show the Echo button on my confirmation pages")
        info("So that my customers can share their purchases with friends")


        scenario("button is requested with no retailer, customer, or purchase info") {
            val count = retailerConfirmationDao.selectRetailerConfirmationCount

            given("a request for the button")
            webDriver.navigate.to(buttonUrl)
//            driver.get(buttonUrl)

            when("there is no other information")
            then("redirect to the button")
            webDriver.getCurrentUrl should equal (buttonRedirectUrl)

            and("no info should be recorded in the database")
            //This is a nasty hack to allow time for the underlying database to be updated.  To repeat this bug start
            //Chamber up in debug mode with no breakpoints set and then run this test, if your machine is like mine the test
            //will pass the first time but fail the second time (of course it should always pass so you will have to force a database update).
            //Anyway, for some reason manually flushing the SQL statement caches does not work...
            Thread.sleep(1000)
            count should equal (retailerConfirmationDao.selectRetailerConfirmationCount)
        }

        scenario("button is requested with invalid retailer id") {
            val count = retailerConfirmationDao.selectRetailerConfirmationCount

            given("a request for the button")
            webDriver.navigate.to(buttonUrl + "?retailerId=foo")

            when("there is an invalid retailer id")
            then("redirect to the button")
            webDriver.getCurrentUrl should equal (buttonRedirectUrl)

            and("no info should be recorded in the database")
            Thread.sleep(1000)
            count should equal (retailerConfirmationDao.selectRetailerConfirmationCount)
        }

        scenario("button is requested from an unknown site") {
            given("a request for the button")
            when("the referrer is an unknown site")
            then("redirect to the button")
            and("no info should be recorded in the database")
            pending
        }

        scenario("button is requested with a valid retailer id") {
            val count = retailerConfirmationDao.selectRetailerConfirmationCount

            given("a request for the button")
            webDriver.navigate.to(buttonUrl + "?retailerId=avalidretailerid")

            when("there is a valid retailer id")
            then("redirect to the button")
            webDriver.getCurrentUrl should equal (buttonRedirectUrl)

            and("record any information given in the database")
            Thread.sleep(1000)
            (count+1) should equal (retailerConfirmationDao.selectRetailerConfirmationCount)
        }

    }

}