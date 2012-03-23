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


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:webIT.xml"))
class FacebookLoginIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = null
    @Autowired @BeanProperty var facebookUserDao: FacebookUserDao = null
    @Autowired @BeanProperty var echoHelper: EchoHelper = null
    @Autowired @BeanProperty var dataCreator: DataCreator = _
    @Autowired @BeanProperty var webDriver: WebDriver = null
    @Autowired @BeanProperty var webDriverUtils: WebDriverUtils = null

    @Autowired @BeanProperty var urls: Properties = null

    new TestContextManager(this.getClass()).prepareTestInstance(this)


    var echoUrl: String = null
    var loginViewUrl: String = null
    var confirmViewUrl: String = null

    {
        echoUrl = urls.getProperty("echoUrl")
        loginViewUrl = urls.getProperty("loginViewUrl")
        confirmViewUrl = urls.getProperty("confirmViewUrl")
        echoUrl != null && loginViewUrl != null && confirmViewUrl != null
    } ensuring (_ == true, "Missing parameters")


    val echoedUser = dataCreator.echoedUser

    def cleanup() {
        webDriverUtils.clearEchoedCookies()
        echoedUserDao.deleteByEmail(echoedUser.email)
        facebookUserDao.deleteByEmail(echoedUser.email)
    }

    override protected def beforeAll() = cleanup()

    override protected def afterAll() = cleanup()


    /* NOTE: This test requires a test user - you can create one as described here: http://developers.facebook.com/docs/test_users/

       Example:
       curl -v 'https://graph.facebook.com/177687295582534/accounts/test-users?access_token=177687295582534|zXC5wmZqodeHhTpUVXThov7zKrA&name=TestUser&permissions=email,publish_stream,offline_access&method=post&installed=true'

       {"id":"100003128184602","access_token":"AAAChmwwiYUYBAJG7MomgcAy1ZCg0fEuXBSjM45n80FV0CHofT1VLZCeGp805f5qt6odHkKBMUwB9n75GJZCrzmbc3nZCDUZBpuxT4WyXliQZDZD","login_url":"https:\/\/www.facebook.com\/platform\/test_account_login.php?user_id=100003128184602&n=R0ZipMc3NCuutvb","email":"testuser_jasdmrk_testuser\u0040tfbnw.net","password":"970285973"}


       User that is does not have the application installed:

       {"id":"100003182349503","access_token":"AAAChmwwiYUYBAFhYSKMYav4FCmBqrE6JyECfScZBZAILmpeHELmIzw5gnMtWDM6WwXJHx7EjKZCP3QdfksZBNqT5LaZAWvo5XVytSKZAZCL5AZDZD","login_url":"https:\/\/www.facebook.com\/platform\/test_account_login.php?user_id=100003182349503&n=QCf99XkcR0vFSGk","email":"testuser_eqhrpby_testuser\u0040tfbnw.net","password":"668228301"}
    */
    feature("A user can echo and create their Echoed account by login via Facebook") {

        info("As a recent purchaser")
        info("I want to be able to click on the Facebook login button")
        info("So that I can echo and create my Echoed account using my Facebook credentials")

        scenario("unknown user clicks on Facebook login button with a valid echoPossibility and is redirected to confirm page post login", IntegrationTest) {

            given("a request to login and echo using Facebook credentials")
            when("the user is unrecognized (no cookie) and with a valid echoPossibility")

            val (ep, count) = echoHelper.setupEchoPossibility(step = "login") //this must match proper step...
            webDriver.get(ep.asUrlParams(prefix = echoUrl + "?", encode = true))


            //NOTE: we are assuming the user has already approved Echoed...
            webDriver.findElement(By.id("facebookLogin")).click()
            webDriver.findElement(By.id("email")).sendKeys(echoedUser.email)
            val pass = webDriver.findElement(By.id("pass"))
            pass.sendKeys(dataCreator.facebookUserPassword)
            pass.submit()

            then("redirect to the echo confirm page")
            webDriver.getCurrentUrl should startWith (echoUrl)

            and("create an EchoedUser account using the Facebook info")
            val facebookUser = facebookUserDao.findByEmail(echoedUser.email)
            facebookUser should not be (null)
            facebookUser.echoedUserId should not be (null)

            val eu = echoedUserDao.findById(facebookUser.echoedUserId)
            eu should not be (null)
            eu.email should be (facebookUser.email)
            eu.facebookUserId should equal (facebookUser.id)

            and("record the Echo in the database")
            echoHelper.validateEchoPossibility(ep.copy(echoedUserId = eu.id, step = "confirm"), count)
        }

    }
}
