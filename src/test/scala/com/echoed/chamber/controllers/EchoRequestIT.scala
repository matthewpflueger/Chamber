package com.echoed.chamber.controllers

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import scala.collection.JavaConversions._
import org.openqa.selenium.{By, WebDriver}
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
    @Autowired @BeanProperty var echoDao: EchoDao = _
    @Autowired @BeanProperty var retailerDao: RetailerDao = _
    @Autowired @BeanProperty var retailerSettingsDao: RetailerSettingsDao = _
    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = _
    @Autowired @BeanProperty var facebookUserDao: FacebookUserDao = _
    @Autowired @BeanProperty var twitterUserDao: TwitterUserDao = _
    @Autowired @BeanProperty var webDriver: WebDriver = _
    @Autowired @BeanProperty var webDriverUtils: WebDriverUtils = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    @Autowired @BeanProperty var urls: Properties = _
    @Autowired @BeanProperty var urlsProperties: Properties = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)


    var echoJsUrl: String = null
    var echoRequestUrl: String = null
    var confirmUrl: String = null
    var echoTestPage: String = null

    {
        echoJsUrl = urls.getProperty("echoJsUrl")
        echoRequestUrl = urls.getProperty("echoRequestUrl")
        confirmUrl = urls.getProperty("confirmUrl")
        echoTestPage = urls.getProperty("echoTestPage")
        echoJsUrl != null && echoRequestUrl != null && confirmUrl != null && echoTestPage != null
    } ensuring(_ == true, "Missing parameters")



    var retailer = dataCreator.retailer
    var retailerSettings = dataCreator.retailerSettings
    var echoedUser = dataCreator.echoedUser
    var twitterUser = dataCreator.twitterUser

    def cleanUp {
        webDriverUtils.clearEchoedCookies()
        webDriverUtils.clearFacebookCookies()
        webDriverUtils.clearTwitterCookies()
        twitterUserDao.deleteByScreenName(twitterUser.screenName)
        retailerDao.deleteByName(retailer.name)
        retailerSettingsDao.deleteByRetailerId(retailer.id)
        echoedUserDao.deleteByEmail(echoedUser.email)
        echoDao.deleteByRetailerId(retailer.id)
    }

    override def beforeAll() {
        assert(retailerSettings.retailerId == retailer.id)
        cleanUp
        retailerDao.insert(retailer)
        retailerSettingsDao.insert(retailerSettings)
    }

    override def afterAll = cleanUp


    feature("A partner's customer can share their purchased items for fun and profit") {

        info("As a customer")
        info("I want to be able to share my purchased items")
        info("So that I can earn rewards")

        scenario("a partner's purchase confirmation page requests the echo js script", IntegrationTest) {
            given("a valid request to get the echo js script")
            when("the partner is active in the system")
            then("return valid javascript to facilitate echoing the purchases")

            val url = "%s?pid=%s" format(echoJsUrl, retailer.id)
            webDriver.get(url)
            webDriver.getPageSource should include(retailer.id)
        }


        scenario("a partner's purchase confirmation page requests items to be echoed", IntegrationTest) {
            given("a request to echo some items")
            when("the request is valid and the partner is active in the system")
            then("return data to facilitate echoing the items by the customer")
            and("record the echo request in the database")

            val echoRequest = """{
                "customerId": "testCustomerId",
                "boughtOn": "2012-02-23T12:08:56.235-0500",
                "orderId": "testOrderId",
                "items": [
                    {
                        "productId": "testProductId11",
                        "productName":  "test product name",
                        "price": 1,
                        "imageUrl": "http://nowhere.com/images/someproduct.png",
                        "landingPageUrl": "http://nowhere.com/someproduct.html",
                        "category": "testcategory",
                        "brand": "Echoed",
                        "description": "My incredibly long description about this particularly stupid product in an attempt to cause errors in using GET with an incredibly long url hopefully hitting the 2K limit"
                    },
                    {
                        "productId": "testProductId22",
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

            try {
                val encryptedRequest = encrypter.encrypt(echoRequest, retailer.secret)

                val url = "%s?pid=%s&data=%s" format(echoRequestUrl, retailer.id, encryptedRequest)
                webDriver.get(url)
                webDriver.getPageSource should include("testProductId1")
                webDriver.getPageSource should include("testProductId2")

                val echoes = echoDao.findByRetailerId(retailer.id)
                echoes should have length(2)
            } finally {
                echoDao.deleteByRetailerId(retailer.id)
            }
        }


        scenario("a partner's purchase confirmation page displays the items to be echoed", IntegrationTest) {
            given("a partner's purchase confirmation page")

            webDriver.get(echoTestPage)
            Thread.sleep(500)

            when("there are items to be shared")
            webDriver.getPageSource should include("test product name")
            val echoes = echoDao.findByRetailerId(retailer.id)
            echoes should have length(2)
            var echo = echoes(0)
            echo.step should equal("request")

            then("click to share the item")
            webDriver.findElement(By.id(echo.id)).click()

            and("show the echo login screen")
            webDriver.switchTo().window(echo.id)
            webDriver.getPageSource should include("test product name")
            echo = echoDao.findById(echo.id)
            echo.step should equal("request,login")

            then("choose Facebook to login and share the item to")
            webDriver.findElement(By.id("facebookLogin")).click()
            echo = echoDao.findById(echo.id)
            echo.step should equal("request,login,authorize-facebook")

            Thread.sleep(500)
            webDriver.findElement(By.id("email")).sendKeys(echoedUser.email)
            var pass = webDriver.findElement(By.id("pass"))
            pass.sendKeys(dataCreator.facebookUserPassword)
            pass.submit()

            then("show the echo confirm page")
            webDriver.getTitle should equal("Confirm")
            webDriver.getPageSource should include("test product name")
            echo = echoDao.findById(echo.id)
            echo.step should equal("request,login,authorize-facebook,confirm")

            val facebookUser = facebookUserDao.findByEmail(echoedUser.email)
            facebookUser should not be (null)
            facebookUser.echoedUserId should not be (null)

            val eu = echoedUserDao.findById(facebookUser.echoedUserId)
            eu should not be (null)
            eu.email should be(facebookUser.email)
            eu.facebookUserId should equal(facebookUser.id)

            and("add Twitter to share the item to")
            webDriver.findElement(By.id("twitterLogin")).click()
            Thread.sleep(500)
            webDriver.findElement(By.id("username_or_email")).sendKeys(twitterUser.screenName)
            pass = webDriver.findElement(By.id("password"))
            pass.sendKeys(dataCreator.twitterPassword)
            pass.submit()
            Thread.sleep(1000)

            and("re-show the echo confirm page")
            webDriver.getTitle should equal("Confirm")
            webDriver.getPageSource should include("test product name")
            echo = echoDao.findById(echo.id)
            echo.step should equal("request,login,authorize-facebook,confirm,authorize-twitter,confirm")

            then("share the item")
            webDriver.findElement(By.id("echoit")).click()
            echo = echoDao.findById(echo.id)
            echo.step should equal("request,login,authorize-facebook,confirm,authorize-twitter,confirm,echoed")

            val ec = echoDao.findById(echo.id)
            ec should not be(null)
            ec.twitterStatusId should not be(null)
            ec.facebookPostId should not be(null)
            webDriver.findElement(By.id("facebookAccount")) should not be(null)
            webDriver.findElement(By.id("twitterAccount")) should not be(null)
        }
    }
}
