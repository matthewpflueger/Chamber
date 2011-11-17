package com.echoed.chamber

import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.openqa.selenium.Cookie
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import org.openqa.selenium.WebDriver
import java.util.Properties
import java.util.Date
import tags.IntegrationTest
import com.echoed.chamber.domain.{FacebookUser, FacebookPost, EchoedUser}
import com.echoed.chamber.dao.{FacebookUserDao, FacebookPostDao, EchoDao, EchoedUserDao}


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:itest.xml"))
class EchoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers {

    @Autowired @BeanProperty var echoDao: EchoDao = _
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
            /*
            curl -v 'https://graph.facebook.com/177687295582534/accounts/test-users?access_token=177687295582534|zXC5wmZqodeHhTpUVXThov7zKrA&name=TestUser&permissions=email,publish_stream,offline_access&method=post&installed=true'

            {"id":"100003128184602","access_token":"AAAChmwwiYUYBAJG7MomgcAy1ZCg0fEuXBSjM45n80FV0CHofT1VLZCeGp805f5qt6odHkKBMUwB9n75GJZCrzmbc3nZCDUZBpuxT4WyXliQZDZD","login_url":"https:\/\/www.facebook.com\/platform\/test_account_login.php?user_id=100003128184602&n=R0ZipMc3NCuutvb","email":"testuser_jasdmrk_testuser\u0040tfbnw.net","password":"970285973"}
            {"id":"100003177284815","access_token":"AAAChmwwiYUYBAKI2bxTrAgnIgLMok1r8Xel3lgBqu0uqR8RtFaxdzXVEzek7MYNlkIxZB4TXcZCZCZBnzM8auZAWZAZCJLNotEhu1tL24ImxAZDZD","login_url":"https:\/\/www.facebook.com\/platform\/test_account_login.php?user_id=100003177284815&n=8L2tMNJBPGMWlAE","email":"testuser_jpmknrv_testuser\u0040tfbnw.net","password":"273385869"}
            */
            val testUserFacebookId = "100003177284815"
            val testUserEmail = "testuser_jpmknrv_testuser@tfbnw.net"
            val testUserAccessToken = "AAAChmwwiYUYBAKI2bxTrAgnIgLMok1r8Xel3lgBqu0uqR8RtFaxdzXVEzek7MYNlkIxZB4TXcZCZCZBnzM8auZAWZAZCJLNotEhu1tL24ImxAZDZD"

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

            val message = "This is my echoed purchase!"
            webDriver.navigate().to("%s?message=%s" format(echoItUrl, message))

            then("show the thank you page")
            webDriver.getTitle should equal ("Thank you")

            and("record the Echo in the database")
            val echo = echoDao.findByEchoPossibilityId(echoPossibility.id)
            echo should not be (null)
            echo.echoedUserId should equal (echoedUser.id)

            val facebookPost: FacebookPost = facebookPostDao.findByEchoId(echo.id)
            facebookPost should not be (null)
            facebookPost.postedOn should not be (null)
            facebookPost.objectId should not be (null)

            and("update the EchoPossibility with the Echo information")
            echoPossibility.echoId = echo.id
            echoPossibility.step = "echoed"
            echoHelper.validateEchoPossibility(echoPossibility, count)
        }

    }

}
