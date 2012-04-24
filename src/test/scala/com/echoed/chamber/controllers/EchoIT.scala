package com.echoed.chamber.controllers

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import scala.collection.JavaConversions
import org.openqa.selenium.{By, Cookie, WebDriver}
import com.echoed.chamber.dao._
import com.echoed.chamber.util.DataCreator
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import java.util.{Properties, Date}
import com.echoed.util.{WebDriverUtils, IntegrationTest}
import org.slf4j.LoggerFactory
import com.echoed.chamber.domain.{EchoMetrics, Echo, FacebookPost}


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:webIT.xml"))
class EchoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    private val logger = LoggerFactory.getLogger(classOf[EchoIT])

    @Autowired @BeanProperty var echoDao: EchoDao = _
    @Autowired @BeanProperty var echoMetricsDao: EchoMetricsDao = _
    @Autowired @BeanProperty var partnerDao: PartnerDao = _
    @Autowired @BeanProperty var partnerSettingsDao: PartnerSettingsDao = _
    @Autowired @BeanProperty var echoClickDao: EchoClickDao = _
    @Autowired @BeanProperty var facebookUserDao: FacebookUserDao = _
    @Autowired @BeanProperty var facebookPostDao: FacebookPostDao = _
    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = _
    @Autowired @BeanProperty var echoHelper: EchoHelper = _
    @Autowired @BeanProperty var webDriver: WebDriver = _
    @Autowired @BeanProperty var webDriverUtils: WebDriverUtils = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    @Autowired @BeanProperty var urls: Properties = _
    @Autowired @BeanProperty var urlsProperties: Properties = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)


    var echoUrl: String = null
    var echoItUrl: String = null
    var loginViewUrl: String = null
    var confirmViewUrl: String = null
    var siteUrl: String = null

    {
        echoUrl = urls.getProperty("echoUrl")
        echoItUrl = urls.getProperty("echoItUrl")
        loginViewUrl = urls.getProperty("loginViewUrl")
        confirmViewUrl = urls.getProperty("confirmViewUrl")
        siteUrl = urlsProperties.getProperty("http.urls.site")
        echoUrl != null && echoItUrl != null && loginViewUrl != null && confirmViewUrl != null && siteUrl != null
    } ensuring (_ == true, "Missing parameters")


    //Set in "a known user clicks to confirm their echo and is directed to thanks for echoing page"
    //Used in the following tests for clicking on the post and tracking the click...
    var echo: Echo = null
    var count: Long = 0
    var facebookPost: FacebookPost = null

    var echoedUser = dataCreator.echoedUser.copy(twitterId = null, twitterUserId = null)
    var facebookUser = dataCreator.facebookUser
    var partner = dataCreator.partner
    var partnerSettings = dataCreator.partnerSettings

    def cleanUp {
        echoedUserDao.deleteByEmail(echoedUser.email)
        echoedUserDao.deleteByScreenName(echoedUser.screenName)
        facebookUserDao.deleteByEmail(facebookUser.email)
        partnerDao.deleteByName(partner.name)
        partnerSettingsDao.deleteByPartnerId(partner.id)
        echoDao.deleteByPartnerId(partner.id)
        echoMetricsDao.deleteByPartnerId(partner.id)
    }

    override def beforeAll() {
        assert(echoedUser.facebookUserId == facebookUser.id)
        assert(partnerSettings.partnerId == partner.id)
        cleanUp
        echoedUserDao.insert(echoedUser)
        facebookUserDao.insertOrUpdate(facebookUser)
        partnerDao.insert(partner)
        partnerSettingsDao.insert(partnerSettings)
    }

    override def afterAll = cleanUp


    feature("A user can share their purchase by clicking on the Echo button on a partner's purchase confirmation page") {

        info("As a recent purchaser")
        info("I want to be able to click on the Echo button")
        info("So that I can share my purchase with friends")

        scenario("user clicks on echo button with invalid parameters and is redirected to Echoed's error page", IntegrationTest) {
            given("a request to echo a purchase")
            when("there is invalid parameters")
            then("redirect to Echoed's error page")
            and("collect information about the partner and order so we may contact the partner to fix")
            pending
        }

        scenario("unknown user clicks on echo button with valid parameters and is redirected to login page", IntegrationTest) {
            given("a request to echo a purchase")
            when("the user is unrecognized (no cookie) and with valid parameters")

            webDriverUtils.clearEchoedCookies()

            val (e, c) = echoHelper.setupEchoPossibility(step = "login")
            echo = e
            count = c
//            val (echoPossibility, count) = echoHelper.setupEchoPossibility(step = "login")
//            webDriver.get(echoPossibility.asUrlParams(prefix = echoUrl + "?", encode = true))
            webDriver.get(echo.asUrlParams(prefix = echoUrl + "?", encode = true))

            then("show the login page")
            webDriver.getTitle should equal ("Login")

            and("record the Echo in the database")
            echo = echoHelper.validateEchoPossibility(echo, count)
//            echo = echoHelper.validateEchoPossibility(echoPossibility, count)
        }

        scenario("a known user clicks on echo button with valid parameters and is redirected to confirmation page", IntegrationTest) {
            echo should not be(null)
//            val (echoPossibility, count) = echoHelper.setupEchoPossibility(
//                    step = "confirm",
//                    echoedUserId = echoedUser.id)

            given("a request to echo a purchase")
            when("the user is recognized (has a cookie) and with valid parameters")
            webDriverUtils.addEchoedUserCookie(echoedUser)
            val echoUrlWithParams = echo.asUrlParams(prefix = echoUrl + "?", encode = true)
//            val echoUrlWithParams = echoPossibility.asUrlParams(prefix = echoUrl + "?", encode = true)
            webDriver.get(echoUrlWithParams)

            then("show the echo confirmation page")
            webDriver.getCurrentUrl should equal (echoUrlWithParams) //we did not redirect...
            webDriver.getTitle should equal ("Confirm")

            and("record the Echo in the database")
            echo = echoHelper.validateEchoPossibility(echo, count)
//            echo = echoHelper.validateEchoPossibility(echoPossibility, count)

        }

        scenario("a known user clicks to confirm their echo and is directed to thanks for echoing page", IntegrationTest) {
            echo should not be(null)
//            val (ep, count) = echoHelper.setupEchoPossibility(
//                    step = "confirm",
//                    echoedUserId = echoedUser.id)
//
//            echoDao.deleteByEchoPossibilityId(ep.id)
//            val em = new EchoMetrics(ep, dataCreator.partnerSettings)
//            echoMetricsDao.insert(em)
//            val echoPossibility = ep.copy(echoMetricsId = em.id)
//            echoDao.insert(echoPossibility)


            given("a request to confirm the echo")
            when("the user is known with a valid echo possibility")
            webDriverUtils.addEchoedUserCookie(echoedUser)

            val postToFacebook = true
            val facebookMessage = "This is my echoed purchase!"
            webDriver.get("%s?postToFacebook=%s&facebookMessage=%s&echoPossibilityId=%s" format(echoItUrl, postToFacebook, facebookMessage, echo.echoPossibilityId))
//            webDriver.get("%s?postToFacebook=%s&facebookMessage=%s&echoPossibilityId=%s" format(echoItUrl, postToFacebook, facebookMessage, echoPossibility.id))

            then("show the thank you page")
            webDriver.getTitle should equal ("Thank you")

            and("record the Echo in the database")
            echo = echoDao.findByEchoPossibilityId(echo.echoPossibilityId)
//            echo = echoDao.findByEchoPossibilityId(echoPossibility.echoPossibilityId)
            echo should not be (null)
            echo.echoedUserId should equal (echoedUser.id)
            echo.echoMetricsId should not be (null)

            val echoMetrics = echoMetricsDao.findByEchoId(echo.id)
            echoMetrics should not be (null)

            echoMetrics.totalClicks should be (0)
            echoMetrics.credit should be > (0f) //not be(null)
            echoMetrics.fee should be > (0f)

            facebookPost = facebookPostDao.findByEchoId(echo.id)
            facebookPost should not be (null)
            facebookPost.facebookId should not be (null)

            and("update the Echo with the Echo information")
            echo = echoHelper.validateEchoPossibility(echo, count) //.copy(echoId = echo.id, step = "echoed"), count)
//            echoHelper.validateEchoPossibility(echoPossibility, count) //.copy(echoId = echo.id, step = "echoed"), count)
        }

    }

    feature("A person can hear the echo by clicking on their friend's post") {

        info("As a person on a social platform")
        info("I want to be able to click on a friend's post")
        info("So that I can go to the partner's site and see/buy the product that my friend purchased")

        scenario("an unknown person clicks on their friend's Facebook post and is redirected to the partner's product/landing page", IntegrationTest) {
            echo should not be (null)
            facebookPost should not be (null)

            webDriverUtils.clearFacebookCookies
            webDriverUtils.clearEchoedCookies()

            given("a click on an Echoed Facebook post")
            webDriver.get(dataCreator.facebookUserLoginPageUrl)
            webDriver.findElement(By.id("email")).sendKeys(dataCreator.facebookUser.email)
            val pass = webDriver.findElement(By.id("pass"))
            pass.sendKeys(dataCreator.facebookUserPassword)
            pass.submit()

            webDriver.get("http://www.facebook.com/profile.php?id=%s&sk=wall" format dataCreator.facebookUser.facebookId)
            val allAnchors = JavaConversions.collectionAsScalaIterable(webDriver.findElements(By.tagName("a")))
            val firstEcho = allAnchors.find(webElement => {
                Option(webElement.getAttribute("href")).orElse(Some("")).map(_.contains(facebookPost.id)).get
            }).get

            when("the user is unknown (no echoedUserId)")
            firstEcho.click

            then("redirect to the partner's landing page")
            val currentWindowHandle = webDriver.getWindowHandle
            for (windowHandle: String <- JavaConversions.asScalaSet(webDriver.getWindowHandles()))
                if (windowHandle != currentWindowHandle) webDriver.switchTo().window(windowHandle)

            try {
                webDriver.getCurrentUrl should startWith(echo.product.landingPageUrl)
            } finally {
                try {
                    if (currentWindowHandle != webDriver.getWindowHandle) {
                        Thread.sleep(1000)
                        webDriver.close
                        webDriver.switchTo.window(currentWindowHandle)
                    }
                } catch {
                    case e => logger.debug("Webdriver may have died", e)
                }
            }

            and("record the EchoClick in the database")
            val echoClick = echoClickDao.findByEchoId(echo.id)
            echoClick should not be (null)
            echoClick.get(0).echoedUserId should be (null)
            echoClick.get(0).facebookPostId should equal (facebookPost.id)
            echoClick.get(0).twitterStatusId should be (null)

            val em = echoMetricsDao.findByEchoId(echoClick.get(0).echoId)
            em.totalClicks should not be (0)
            em.credit should be > 0f
            em.fee should be > 0f
        }

    }
}
