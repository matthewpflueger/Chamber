package com.echoed.chamber.controllers

import com.echoed.chamber.dao.{FacebookUserDao, EchoedUserDao}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import java.util.Properties
import org.openqa.selenium.{By, WebDriver}
import com.echoed.util.{WebDriverUtils, IntegrationTest}
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import com.echoed.chamber.util.DataCreator
import scala.collection.JavaConversions


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:webIT.xml"))
class FacebookAppIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = _
    @Autowired @BeanProperty var facebookUserDao: FacebookUserDao = _
    @Autowired @BeanProperty var echoHelper: EchoHelper = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _
    @Autowired @BeanProperty var webDriver: WebDriver = _
    @Autowired @BeanProperty var webDriverUtils: WebDriverUtils = _
    @Autowired @BeanProperty var facebookAccessProperties: Properties = _

    @Autowired @BeanProperty var urls: Properties = null

    new TestContextManager(this.getClass()).prepareTestInstance(this)


    var closetUrl: String = null

    {
        closetUrl = urls.getProperty("closetUrl")
        closetUrl != null
    } ensuring (_ == true, "Missing parameters")


    val echoedUser = dataCreator.echoedUser
    val facebookUser = dataCreator.facebookUser

    def cleanup() {
        webDriverUtils.clearFacebookCookies()
        webDriverUtils.clearEchoedCookies()
        echoedUserDao.deleteByEmail(echoedUser.email)
        facebookUserDao.deleteByEmail(echoedUser.email)
    }

    override protected def beforeAll() = cleanup()

    override protected def afterAll() = cleanup()


    /*
       This is a valid signed_request parameter for our user: testuser_jpmknrv_testuser@tfbnw.net
       ef_uO0QXk4OvSQA1kno1SNq9LwOxqRggqdjECMZQYjk.eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImV4cGlyZXMiOjAsImlzc3VlZF9hdCI6MTMyNzQzNzU5Mywib2F1dGhfdG9rZW4iOiJBQUFDaG13d2lZVVlCQURrMktEb0RtbFZaQ3RKOVhSMVR4cmFYa1FsY2hVdnpNd2Z4cmN1U1laQWtuOEVFdGM0UW96VnQ2VERIZ2haQlNDTUFJeEVWYUxPYmdiVTVBSHdxOFZlOW9BRm9nWkRaRCIsInVzZXIiOnsiY291bnRyeSI6InVzIiwibG9jYWxlIjoiZW5fVVMiLCJhZ2UiOnsibWluIjoyMX19LCJ1c2VyX2lkIjoiMTAwMDAzMTc3Mjg0ODE1In0
    */
    feature("A user can view their exhibt in Facebook") {

        info("As a Facebook user")
        info("I want to be able to click on the View Echoed Exhibit action link")
        info("So that I can view the exhibit right within Facebook")

        scenario("unknown Facebook user clicks on View Echoed Exhibit and shown the exhibit right within Facebook", IntegrationTest) {

            given("a request view the Echoed Exhibit from Facebook")
            when("the user is unrecognized (no cookie) but with valid Facebook credentials (signed request)")

            webDriver.get("http://www.facebook.com")

            webDriver.findElement(By.id("email")).sendKeys(echoedUser.email)
            val pass = webDriver.findElement(By.id("pass"))
            pass.sendKeys(dataCreator.facebookUserPassword)
            pass.submit()

            webDriver.get("http://www.facebook.com/profile.php?id=%s&sk=wall" format dataCreator.facebookUser.facebookId)
            val allAnchors = JavaConversions.collectionAsScalaIterable(webDriver.findElements(By.tagName("a")))
            val url = facebookAccessProperties.getProperty("canvasApp")
            val firstEcho = allAnchors.find(webElement => {
                Option(webElement.getAttribute("href")).map(_.startsWith(url)).getOrElse(false)
            }).get

            when("the user is unknown")
            firstEcho.click

            then("show the exhibit")
            webDriver.switchTo().frame("iframe_canvas")
            webDriver.getCurrentUrl should startWith (closetUrl)

            and("create an EchoedUser account using the Facebook info")
            val facebookUser = facebookUserDao.findByEmail(echoedUser.email)
            facebookUser should not be (null)
            facebookUser.echoedUserId should not be (null)

            val eu = echoedUserDao.findById(facebookUser.echoedUserId)
            eu should not be (null)
            eu.email should be (facebookUser.email)
            eu.facebookUserId should equal (facebookUser.id)
        }

    }
}
