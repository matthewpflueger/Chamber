package com.echoed.chamber.controllers

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import scala.collection.JavaConversions._
import org.openqa.selenium.{By, WebDriver}
import com.echoed.chamber.domain.{Echo, FacebookPost}
import com.echoed.chamber.dao._
import com.echoed.chamber.util.DataCreator
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import java.util.Properties
import org.slf4j.LoggerFactory
import com.echoed.util.{Encrypter, WebDriverUtils, IntegrationTest}


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:webIT.xml"))
class EchoRequestIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    private val logger = LoggerFactory.getLogger(classOf[EchoRequestIT])

    @Autowired @BeanProperty var encrypter: Encrypter = _
    @Autowired @BeanProperty var echoPossibilityDao: EchoPossibilityDao = _
    @Autowired @BeanProperty var retailerDao: RetailerDao = _
    @Autowired @BeanProperty var retailerSettingsDao: RetailerSettingsDao = _
    @Autowired @BeanProperty var webDriver: WebDriver = _
    @Autowired @BeanProperty var webDriverUtils: WebDriverUtils = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    @Autowired @BeanProperty var urls: Properties = _
    @Autowired @BeanProperty var urlsProperties: Properties = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)


    var echoJsUrl: String = null
    var echoRequestUrl: String = null

    {
        echoJsUrl = urls.getProperty("echoJsUrl")
        echoRequestUrl = urls.getProperty("echoRequestUrl")
        echoJsUrl != null && echoRequestUrl != null
    } ensuring(_ == true, "Missing parameters")



    var retailer = dataCreator.retailer
    var retailerSettings = dataCreator.retailerSettings

    def cleanUp {
        retailerDao.deleteByName(retailer.name)
        retailerSettingsDao.deleteByRetailerId(retailer.id)
        echoPossibilityDao.deleteByRetailerId(retailer.id)
    }

    override def beforeAll() {
        assert(retailerSettings.retailerId == retailer.id)
        cleanUp
        retailerDao.insert(retailer)
        retailerSettingsDao.insert(retailerSettings)
    }

    override def afterAll = cleanUp


    feature("A partner's customer can request items to be Echoed") {

        info("As a customer")
        info("I want to be able to request items to be echoed")
        info("So that I can share my purchase with friends")

        scenario("a customer is shown a purchase confirmation page which requests the echo js script", IntegrationTest) {
            given("a valid request to get the echo js script")
            when("the partner is active in the system")
            then("return valid javascript to facilitate echoing the purchases")

            val url = "%s?pid=%s" format(echoJsUrl, retailer.id)
            webDriver.get(url)
            webDriver.getPageSource should include(retailer.id)
        }


        scenario("the a purchase confirmation page requests for items items to be echoed", IntegrationTest) {
            given("a request to echo some items")
            when("the request is valid and the partner is active in the system")
            then("return data to facilitate echoing the items by the consumer")
            and("record the echo request in the database")
//            "imageUrl": "http://nowhere.com"
// @BeanProperty var productId: String = _
//    @BeanProperty var productName: String = _
//    @BeanProperty var category: String = _
//    @BeanProperty var brand: String = _
//    @BeanProperty var price: Float = 0
//    @BeanProperty var imageUrl: String = _
//    @BeanProperty var landingPageUrl: String = _
//    @BeanProperty var description: String = _
            val echoRequest = """{
                "customerId": "testCustomerId",
                "boughtOn": "2012-02-23T12:08:56.235-0500",
                "orderId": "testOrderId",
                "items": [
                    {
                        "productId": "testProductId1",
                        "productName":  "test product name",
                        "price": 1,
                        "imageUrl": "http://nowhere.com/images/someproduct.png",
                        "landingPageUrl": "http://nowhere.com/someproduct.html",
                        "category": "testcategory",
                        "brand": "Echoed",
                        "description": "My incredibly long description about this particularly stupid product in an attempt to cause errors in using GET with an incredibly long url hopefully hitting the 2K limit"
                    },
                    {
                        "productId": "testProductId2",
                        "productName":  "test product name",
                        "price": 1,
                        "imageUrl": "http://nowhere.com/images/someproduct.png",
                        "landingPageUrl": "http://nowhere.com/someproduct.html",
                        "category": "testcategory",
                        "brand": "Echoed",
                        "description": "My incredibly long description about this particularly stupid product in an attempt to cause errors in using GET with an incredibly long url hopefully hitting the 2K limit"
                    }
                ]
            }"""

            val encryptedRequest = encrypter.encrypt(echoRequest, retailer.secret)

            val url = "%s?pid=%s&data=%s" format(echoRequestUrl, retailer.id, encryptedRequest)
            webDriver.get(url)
            webDriver.getPageSource should include("testProductId1")
            webDriver.getPageSource should include("testProductId2")

            val echoes = echoPossibilityDao.findByRetailerId(retailer.id)
            echoes should have length(2)
        }


    }
}
