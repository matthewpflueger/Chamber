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
import org.apache.ibatis.session.SqlSession


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:database.xml"))
class EchoButtonIT extends FeatureSpec with GivenWhenThen with ShouldMatchers {

    @Autowired @BeanProperty var retailerConfirmationDao: RetailerConfirmationDao = null
    new TestContextManager(this.getClass()).prepareTestInstance(this)


    feature("An Echo button is shown on a retailer's purchase confirmation page") {

        info("As a recent purchaser")
        info("I want to be able to click on the Echo button")
        info("So that I can share my purchase with friends")


        scenario("button is requested with no retailer, customer, or purchase info") {
            val count = retailerConfirmationDao.selectRetailerConfirmationCount

            given("a request for the button")
            val driver = new FirefoxDriver()
            driver.get("http://v1-api.echoed.com/echo/button")

            when("there is no other information")
            then("redirect to the button")
            driver.getCurrentUrl should equal ("http://v1-cdn.echoed.com/button_echoed.png")

            and("no info should be recorded in the database")
            //This is a nasty hack to allow time for the underlying database to be updated.  To repeat this bug start
            // Chamber up in debug mode with no breakpoints set and then run this test, if your machine is like mine the test
            //will pass the first time but fail the second time (of course it should always pass so you will have to force a database update).
            //Anyway, for some reason manually flushing the SQL statement caches does not work...
            Thread.sleep(1000)
            count should equal (retailerConfirmationDao.selectRetailerConfirmationCount)
        }

    }

}