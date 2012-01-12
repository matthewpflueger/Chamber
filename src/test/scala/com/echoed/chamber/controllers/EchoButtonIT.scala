package com.echoed.chamber.controllers

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import org.openqa.selenium.WebDriver
import java.util.Properties
import org.slf4j.LoggerFactory
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import com.echoed.util.{IntegrationTest, CookieValidator}
import com.echoed.chamber.util.DataCreator
import com.echoed.chamber.dao.{RetailerSettingsDao, RetailerDao}


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:webIT.xml"))
class EchoButtonIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    private final val logger = LoggerFactory.getLogger(classOf[EchoButtonIT])

    @Autowired @BeanProperty var retailerDao: RetailerDao = _
    @Autowired @BeanProperty var retailerSettingsDao: RetailerSettingsDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _
    @Autowired @BeanProperty var echoHelper: EchoHelper = _
    @Autowired @BeanProperty var webDriver: WebDriver = _

    @Autowired @BeanProperty var urls: Properties = null

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val retailer = dataCreator.retailer
    val retailerSettings = dataCreator.retailerSettings

    var buttonUrl: String = null
    var buttonViewUrl: String = null

    {
        buttonUrl = urls.getProperty("buttonUrl")
        buttonViewUrl = urls.getProperty("buttonViewUrl")
        buttonUrl != null && buttonViewUrl != null
    } ensuring (_ == true, "Missing parameters")

    def cleanUp {
        retailerDao.deleteByName(retailer.name)
        retailerSettingsDao.deleteByRetailerId(retailer.id)
    }

    override def beforeAll() {
        cleanUp
        retailerDao.insert(retailer)
        retailerSettingsDao.insert(retailerSettings)
    }

    override def afterAll = cleanUp

    feature("An Echo button is shown on a retailer's purchase confirmation page") {

        info("As a retailer")
        info("I want to be able to show the Echo button on my confirmation pages")
        info("So that my customers can share their purchases with friends")


        scenario("button is requested with no retailer, customer, or purchase info", IntegrationTest) {
            val count = echoHelper.getEchoPossibilityCount

            given("a request for the button")
            webDriver.get(buttonUrl)

            when("there is no other information")
            then("redirect to the button")
            webDriver.getCurrentUrl should equal (buttonViewUrl)

            and("there be an echoPossibility cookie with no value")
            CookieValidator.validateNoCookie(webDriver, "echoPossibility")

            and("no info should be recorded in the database")
            echoHelper.validateCountIs(count)
        }

        scenario("button is requested with invalid retailer id", IntegrationTest) {
            val fooRetailer = retailer.copy(id = "foo")
            val fooRetailerSettings = retailerSettings.copy(retailerId = fooRetailer.id)

            def deleteRetailer {
                retailerDao.deleteById(fooRetailer.id)
                retailerSettingsDao.deleteByRetailerId(fooRetailer.id)
            }

            try {
                val (echoPossibility, count) = echoHelper.setupEchoPossibility(
                        retailer = fooRetailer,
                        retailerSettings = fooRetailerSettings)

                deleteRetailer

                given("a request for the button")
                webDriver.get(echoPossibility.asUrlParams(prefix = buttonUrl + "?", encode = true))

                when("there is an invalid retailer id")
                then("redirect to the button")
                webDriver.getCurrentUrl should equal (buttonViewUrl)

                and("no info should be recorded in the database")
                echoHelper.validateCountIs(count)
            } finally {
                deleteRetailer
            }
        }

        scenario("button is requested with valid parameters", IntegrationTest) {
            val (echoPossibility, count) = echoHelper.setupEchoPossibility(retailer)

            given("a request for the button")
            webDriver.get(echoPossibility.asUrlParams(prefix = buttonUrl + "?", encode = true))

            when("there are valid parameters")
            then("redirect to the button")
            webDriver.getCurrentUrl should equal (buttonViewUrl)

            and("record the EchoPossibility in the database")
            echoHelper.validateEchoPossibility(echoPossibility, count)
        }

    }

}

