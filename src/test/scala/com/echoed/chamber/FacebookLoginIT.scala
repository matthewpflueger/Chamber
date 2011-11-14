package com.echoed.chamber

import dao.{FacebookUserDao, EchoedUserDao}
import domain.EchoedUser
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.scalatest.matchers.ShouldMatchers
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import tags.IntegrationTest
import java.util.{Date, Properties}
import org.openqa.selenium.{By, Cookie, WebDriver}


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:itest.xml"))
class FacebookLoginIT extends FeatureSpec with GivenWhenThen with ShouldMatchers {

    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = null
    @Autowired @BeanProperty var facebookUserDao: FacebookUserDao = null
    @Autowired @BeanProperty var echoHelper: EchoHelper = null
    @Autowired @BeanProperty var webDriver: WebDriver = null

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


    /* NOTE: This test requires a test user - you can create one as described here: http://developers.facebook.com/docs/test_users/

       Example:
       curl -v 'https://graph.facebook.com/177687295582534/accounts/test-users?access_token=177687295582534|zXC5wmZqodeHhTpUVXThov7zKrA&name=TestUser&permissions=email,publish_stream,offline_access&method=post'

       {"id":"100003182349503","access_token":"AAAChmwwiYUYBAFhYSKMYav4FCmBqrE6JyECfScZBZAILmpeHELmIzw5gnMtWDM6WwXJHx7EjKZCP3QdfksZBNqT5LaZAWvo5XVytSKZAZCL5AZDZD","login_url":"https:\/\/www.facebook.com\/platform\/test_account_login.php?user_id=100003182349503&n=QCf99XkcR0vFSGk","email":"testuser_eqhrpby_testuser\u0040tfbnw.net","password":"668228301"}
    */
    feature("A user can echo and create their Echoed account by login via Facebook") {

        info("As a recent purchaser")
        info("I want to be able to click on the Facebook login button")
        info("So that I can echo and create my Echoed account using my Facebook credentials")

        scenario("unknown user clicks on Facebook login button with a valid echoPossibility and is redirected to confirm page post login", IntegrationTest) {
            val testUserFacebookId = "100003076656188"
            val testUserEmail = "tech@echoed.com"
            val testUserPass = "etech25"

            echoedUserDao.deleteByEmail(testUserEmail)
            facebookUserDao.deleteByEmail(testUserEmail)

            given("a request to login and echo using Facebook credentials")
            when("the user is unrecognized (no cookie) and with a valid echoPossibility")



            val (echoPossibility, count) = echoHelper.setupEchoPossibility(step = "login") //this must match proper step...
            webDriver.manage().deleteCookieNamed("echoedUserId")
            webDriver.navigate.to(echoUrl + echoPossibility.generateUrlParameters)
            webDriver.getCurrentUrl should equal (loginViewUrl)

            //NOTE: we are assuming the user already has a valid Facebook session and has already approved Echoed...
            webDriver.findElement(By.id("facebookLogin")).click()
            webDriver.findElement(By.id("email")).sendKeys(testUserEmail)
            val pass = webDriver.findElement(By.id("pass"))
            pass.sendKeys(testUserPass)
            pass.submit()


            then("redirect to the confirm page")
            webDriver.getCurrentUrl.startsWith(confirmViewUrl) should be (true)

            and("create an EchoedUser account using the Facebook info")
            val echoedUser = echoedUserDao.findByFacebookUserId(testUserFacebookId)
            echoedUser.email should be (testUserEmail)

            val facebookUser = facebookUserDao.findById(echoedUser.facebookUserId)
            facebookUser.email should be (testUserEmail)
            facebookUser.echoedUserId should equal (echoedUser.id)

            and("record the EchoPossibility in the database")
            echoPossibility.step = "confirm"
            echoHelper.validateEchoPossibility(echoPossibility, count)
        }

    }
}
