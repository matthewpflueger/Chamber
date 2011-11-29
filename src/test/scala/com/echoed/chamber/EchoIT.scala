package com.echoed.chamber

import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import java.util.Properties
import java.util.Date
import tags.IntegrationTest
import scala.collection.JavaConversions
import org.openqa.selenium.{By, Cookie, WebDriver}
import com.echoed.chamber.domain.{Echo, FacebookUser, FacebookPost, EchoedUser}
import com.echoed.chamber.dao._


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:itest.xml"))
class EchoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers {

    @Autowired @BeanProperty var echoDao: EchoDao = _
    @Autowired @BeanProperty var echoClickDao: EchoClickDao = _
    @Autowired @BeanProperty var facebookUserDao: FacebookUserDao = _
    @Autowired @BeanProperty var facebookPostDao: FacebookPostDao = _
    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = _
    @Autowired @BeanProperty var echoHelper: EchoHelper = _
    @Autowired @BeanProperty var webDriver: WebDriver = _

    @Autowired @BeanProperty var urls: Properties = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)


    var echoUrl: String = null
    var echoItUrl: String = null
    var loginViewUrl: String = null
    var confirmViewUrl: String = null

    {
        echoUrl = urls.getProperty("echoUrl")
        echoItUrl = urls.getProperty("echoItUrl")
        loginViewUrl = urls.getProperty("loginViewUrl")
        confirmViewUrl = urls.getProperty("confirmViewUrl")
        echoUrl != null && echoItUrl != null && loginViewUrl != null && confirmViewUrl != null
    } ensuring (_ == true, "Missing parameters")

    /*
        curl -v 'https://graph.facebook.com/177687295582534/accounts/test-users?access_token=177687295582534|zXC5wmZqodeHhTpUVXThov7zKrA&name=TestUser&permissions=email,publish_stream,offline_access&method=post&installed=true'

        //OLD {"id":"100003128184602","access_token":"AAAChmwwiYUYBAJG7MomgcAy1ZCg0fEuXBSjM45n80FV0CHofT1VLZCeGp805f5qt6odHkKBMUwB9n75GJZCrzmbc3nZCDUZBpuxT4WyXliQZDZD","login_url":"https:\/\/www.facebook.com\/platform\/test_account_login.php?user_id=100003128184602&n=R0ZipMc3NCuutvb","email":"testuser_jasdmrk_testuser\u0040tfbnw.net","password":"970285973"}
        {"id":"100003177284815","access_token":"AAAChmwwiYUYBAKI2bxTrAgnIgLMok1r8Xel3lgBqu0uqR8RtFaxdzXVEzek7MYNlkIxZB4TXcZCZCZBnzM8auZAWZAZCJLNotEhu1tL24ImxAZDZD","login_url":"https:\/\/www.facebook.com\/platform\/test_account_login.php?user_id=100003177284815&n=8L2tMNJBPGMWlAE","email":"testuser_jpmknrv_testuser\u0040tfbnw.net","password":"273385869"}
    */
    val testUserFacebookId = "100003177284815"
    val testUserEmail = "testuser_jpmknrv_testuser@tfbnw.net"
    val testUserPassword = "273385869"
    val testUserAccessToken = "AAAChmwwiYUYBAKI2bxTrAgnIgLMok1r8Xel3lgBqu0uqR8RtFaxdzXVEzek7MYNlkIxZB4TXcZCZCZBnzM8auZAWZAZCJLNotEhu1tL24ImxAZDZD"
    val testUserLoginPageUrl = "https://www.facebook.com/platform/test_account_login.php?user_id=100003177284815&n=8L2tMNJBPGMWlAE"

    //Set in "a known user clicks to confirm their echo and is directed to thanks for echoing page"
    //Used in the following tests for clicking on the post and tracking the click...
    var echo: Echo = null
    var facebookPost: FacebookPost = null


    feature("A user can share their purchase by clicking on the Echo button on a retailer's purchase confirmation page") {

        info("As a recent purchaser")
        info("I want to be able to click on the Echo button")
        info("So that I can share my purchase with friends")

        scenario("user clicks on echo button with invalid parameters and is redirected to Echoed's error page", IntegrationTest) {
            given("a request to echo a purchase")
            when("there is invalid parameters")
            then("redirect to Echoed's error page")
            and("collect information about the retailer and order so we may contact the retailer to fix")
            pending
        }

        scenario("unknown user clicks on echo button with valid parameters and is redirected to login page", IntegrationTest) {
            given("a request to echo a purchase")
            when("the user is unrecognized (no cookie) and with valid parameters")
            val (echoPossibility, count) = echoHelper.setupEchoPossibility(step = "login") //this must match proper step...
            webDriver.manage().deleteCookieNamed("echoedUserId")
            webDriver.navigate.to(echoUrl + echoPossibility.generateUrlParameters)

            then("redirect to the login page")
            webDriver.getCurrentUrl should equal (loginViewUrl)

            and("record the EchoPossibility in the database")
            echoHelper.validateEchoPossibility(echoPossibility, count)
        }

        scenario("a known user clicks on echo button with valid parameters and is redirected to confirmation page", IntegrationTest) {
            val echoedUser = new EchoedUser(null, "matthew.pflueger", "matthew.pflueger@gmail.com", "Matthew", "Pflueger", null, null)
            echoedUserDao.deleteByEmail("matthew.pflueger@gmail.com")
            echoedUserDao.insert(echoedUser)
            val (echoPossibility, count) = echoHelper.setupEchoPossibility(step = "confirm", echoedUserId = echoedUser.id)

            given("a request to echo a purchase")
            when("the user is recognized (has a cookie) and with valid parameters")
            val cookie = new Cookie.Builder("echoedUserId", echoedUser.id)
                    .domain(".echoed.com")
                    .path("/")
                    .expiresOn(new Date((new Date().getTime + (1000*60*60*24))))
                    .build()
            webDriver.manage().addCookie(cookie)
            val echoUrlWithParams = echoUrl + echoPossibility.generateUrlParameters
            webDriver.navigate().to(echoUrlWithParams)

            then("redirect to the echo confirmation page")
            webDriver.getCurrentUrl should equal (echoUrlWithParams) //we did not redirect...
            webDriver.getTitle should equal ("Popup")

            and("record the EchoPossibility in the database")
            echoHelper.validateEchoPossibility(echoPossibility, count)

        }

        scenario("a known user clicks to confirm their echo and is directed to thanks for echoing page", IntegrationTest) {

            val echoedUser = new EchoedUser(null, null, testUserEmail, "TestUser", "TestUser", testUserFacebookId, null)
            echoedUserDao.deleteByEmail(testUserEmail)
            echoedUserDao.insert(echoedUser)
            val facebookUser = new FacebookUser(testUserFacebookId, "TestUser", "TestUser", "", "male", testUserEmail, "", "")
            facebookUser.accessToken = testUserAccessToken
            facebookUser.echoedUserId = echoedUser.id
            facebookUserDao.insertOrUpdate(facebookUser)

            val (echoPossibility, count) = echoHelper.setupEchoPossibility(step = "confirm", echoedUserId = echoedUser.id)
            echoHelper.echoPossibilityDao.insertOrUpdate(echoPossibility)
            echoDao.deleteByEchoPossibilityId(echoPossibility.id)


            given("a request to confirm the echo")
            when("the user is known with a valid echo possibility")
            val echoedUserIdCookie = new Cookie.Builder("echoedUserId", echoedUser.id)
                    .domain(".echoed.com")
                    .path("/")
                    .expiresOn(new Date((new Date().getTime + (1000*60*60*24))))
                    .build()
            val echoPossibilityCookie = new Cookie.Builder("echoPossibility", echoPossibility.id)
                    .domain(".echoed.com")
                    .path("/")
                    .expiresOn(new Date((new Date().getTime + (1000*60*60*24))))
                    .build()
            webDriver.navigate.to("http://www.echoed.com")
            webDriver.manage().addCookie(echoedUserIdCookie)
            webDriver.manage().addCookie(echoPossibilityCookie)

            val postToFacebook = true
            val facebookMessage = "This is my echoed purchase!"
            webDriver.navigate().to("%s?postToFacebook=%s&facebookMessage=%s" format(echoItUrl, postToFacebook, facebookMessage))

            then("show the thank you page")
            webDriver.getTitle should equal ("Thank you")

            and("record the Echo in the database")
            echo = echoDao.findByEchoPossibilityId(echoPossibility.id)
            echo should not be (null)
            echo.echoedUserId should equal (echoedUser.id)

            facebookPost = facebookPostDao.findByEchoId(echo.id)
            facebookPost should not be (null)
            facebookPost.postedOn should not be (null)
            facebookPost.objectId should not be (null)

            and("update the EchoPossibility with the Echo information")
            echoPossibility.echoId = echo.id
            echoPossibility.step = "echoed"
            echoHelper.validateEchoPossibility(echoPossibility, count)
        }

    }

    feature("A person can hear the echo by clicking on their friend's post") {

        info("As a person on a social platform")
        info("I want to be able to click on a friend's post")
        info("So that I can go to the retailer's site and see/buy the product that my friend purchased")

        scenario("an unknown person clicks on their friend's Facebook post and is redirected to the retailer's product/landing page", IntegrationTest) {
            echo should not be (null)
            facebookPost should not be (null)

            //satisfies the when("the user is unknown (no echoedUserId") below...
            webDriver.navigate().to("http://www.echoed.com")
            webDriver.manage().deleteAllCookies()

            given("a click on an Echoed Facebook post")
            webDriver.navigate.to(testUserLoginPageUrl)
            webDriver.findElement(By.id("email")).sendKeys(testUserEmail)
            val pass = webDriver.findElement(By.id("pass"))
            pass.sendKeys(testUserPassword)
            pass.submit()

            webDriver.navigate.to("http://www.facebook.com/profile.php?id=100003177284815&sk=wall")
            val allAnchors = JavaConversions.collectionAsScalaIterable(webDriver.findElements(By.tagName("a")))
            val url = "http://v1-api.echoed.com/echo/%s/%s" format(echo.id, facebookPost.id)
            val firstEcho = allAnchors.find(webElement => {
                webElement.getAttribute("href") == url
            }).get

            when("the user is unknown (no echoedUserId)")
            firstEcho.click

            then("redirect to the retailer's landing page")
            val currentWindowHandle = webDriver.getWindowHandle
            for (windowHandle: String <- JavaConversions.asScalaSet(webDriver.getWindowHandles()))
                if (windowHandle != currentWindowHandle) webDriver.switchTo().window(windowHandle)

            webDriver.getCurrentUrl should startWith(echo.landingPageUrl)

            and("record the EchoClick in the database")
            val echoClick = echoClickDao.findByEchoId(echo.id)
            echoClick should not be (null)
            echoClick.echoedUserId should be (null)
            echoClick.facebookPostId should equal (facebookPost.id)
            echoClick.twitterStatusId should be (null)

        }

        scenario("a known person clicks on their friend's Facebook post and is redirected to the retailer's product/landing page", IntegrationTest) {
            given("a click on an Echoed Facebook post")
            when("the user is known (has an echoedUserId)")
            then("redirect to the retailer's landing page")
            and("record the EchoClick in the database")
            pending
        }

        scenario("a known Echoed user clicks on their own Facebook post and is redirected to the retailer's product/landing page", IntegrationTest) {
            given("a click on an Echoed Facebook post")
            when("the user is known (has an echoedUserId) and the Facebook post is theirs")
            then("redirect to the retailer's landing page")
            and("do not record the EchoClick in the database")
            pending
        }

    }
}
