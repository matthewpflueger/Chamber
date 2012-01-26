package com.echoed.chamber.controllers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import org.openqa.selenium.{By, WebDriver}
import com.echoed.chamber.util.DataCreator
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import java.util.{UUID, Properties}
import com.echoed.chamber.dao.{FacebookUserDao, EchoedUserDao, TwitterUserDao}
import com.echoed.util.IntegrationTest
import com.echoed.util.WebDriverUtils._


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:webIT.xml"))
class FacebookAddIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = _
    @Autowired @BeanProperty var facebookUserDao: FacebookUserDao = _
    @Autowired @BeanProperty var twitterUserDao: TwitterUserDao = _
    @Autowired @BeanProperty var echoHelper: EchoHelper = _
    @Autowired @BeanProperty var webDriver: WebDriver = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _
    @Autowired @BeanProperty var urls: Properties = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)


    var closetUrl: String = null
    var apiUrl: String = null

    {
        closetUrl = urls.getProperty("closetUrl")
        apiUrl = urls.getProperty("apiUrl")
        closetUrl != null && apiUrl != null
    } ensuring (_ == true, "Missing parameters")


    val echoedUser = dataCreator.echoedUser.copy(
        id = UUID.randomUUID.toString,
        facebookUserId = null,
        facebookId = null,
        twitterUserId = null,
        twitterId = null,
        email = "2ociv0134nv@echoed.com",
        screenName = null)
    val facebookUser = dataCreator.facebookUser.copy(id = UUID.randomUUID.toString)

    def cleanup() {
        facebookUserDao.deleteByEmail(facebookUser.email)
        echoedUserDao.deleteByEmail(dataCreator.echoedUser.email)
        echoedUserDao.deleteByEmail(echoedUser.email)
        echoedUserDao.deleteByScreenName(echoedUser.screenName)
    }

    override protected def beforeAll() {
        cleanup()
        echoedUserDao.insert(echoedUser)
    }

    override protected def afterAll() {
        cleanup()
    }

    var firstScenarioPassed = false

    feature("A user can add their Facebook account to their existing Echoed user account") {

        info("As an Echoed user")
        info("I want to be able to click on the add Facebook account button")
        info("So that I can add my Facebook account to my Echoed account")

        scenario("user clicks on add Facebook account button and successfully adds their Facebook account to their Echoed account", IntegrationTest) {

            given("a request to add their Facebook account to their Echoed account")
            clearFacebookCookies(webDriver)
            navigateToCloset(webDriver, echoedUser)

            when("the user has no associated Facebook account and their Facebook credentials matches no existing Twitter account")
            then("send them to Facebook to login")
            webDriver.findElement(By.id("addFacebookLink")).click
            webDriver.findElement(By.id("email")).sendKeys(facebookUser.email)
            val pass = webDriver.findElement(By.id("pass"))
            pass.sendKeys(dataCreator.facebookUserPassword)
            pass.submit()


            and("add their Facebook account to their Echoed user account")
            webDriver.getTitle() should be ("My Exhibit")
            webDriver.findElement(By.id("facebookAccount")) should not be (null)
            val eu = echoedUserDao.findByFacebookId(facebookUser.facebookId)
            eu should not be (null)
            firstScenarioPassed = true
        }


        scenario("user clicks on add Facebook account button and tries to add existing Facebook account to Echoed user account", IntegrationTest) {
            firstScenarioPassed should be (true)

            val echoedUser2 = echoedUser.copy(
                    id = UUID.randomUUID.toString,
                    facebookUserId = null,
                    facebookId = null,
                    twitterUserId = null,
                    twitterId = null,
                    email = "190sjnv0123@echoed.com",
                    screenName = null)
            try {
                echoedUserDao.insert(echoedUser2)


                given("a request to add their Facebook account to their Echoed account")

                clearEchoedCookies(webDriver)

                navigateToCloset(webDriver, echoedUser2)

                when("the user has no associated Facebook account and their Facebook credentials matches an existing Facebook account")
                then("send them to Facebook to login")
                webDriver.findElement(By.id("addFacebookLink")).click



                and("do not add the Facebook account")
                webDriver.getTitle should be ("My Exhibit")
                evaluating { webDriver.findElement(By.id("facebookAccount")) } should produce [org.openqa.selenium.NoSuchElementException]
                webDriver.findElement(By.id("addFacebookLink")) should not be(null)

                and("show them an error explanation")
                webDriver.getPageSource should include ("Facebook account already in use")
            } finally {
                echoedUserDao.deleteByEmail(echoedUser2.email)
            }
        }

        scenario("user tries to add their own, already existing Facebook account to Echoed user account", IntegrationTest) {
            firstScenarioPassed should be (true)

            clearFacebookCookies(webDriver)
            clearEchoedCookies(webDriver)
            navigateToCloset(webDriver, echoedUser)

            given("a request to add their own, already existing Facebook account to their Echoed account")
            webDriver.get("https://www.facebook.com/dialog/oauth?client_id=177687295582534&redirect_uri=http://v1-api.echoed.com/facebook/add?redirect=closet&scope=email,publish_stream,offline_access")

//            webDriver.get(apiUrl)
//            webDriver.getTitle should startWith("Echoed")

            when("the user has an associated Facebook account")
            then("send them to Facebook to login")
//            webDriver.findElement(By.id("facebookLogin")).click
            webDriver.findElement(By.id("email")).sendKeys(facebookUser.email)
            val pass = webDriver.findElement(By.id("pass"))
            pass.sendKeys(dataCreator.facebookUserPassword)
            pass.submit()


            and("re-assign their Facebook account to their Echoed user account")
            webDriver.getTitle() should be ("My Exhibit")
            webDriver.findElement(By.id("facebookAccount")) should not be (null)
            val eu = echoedUserDao.findByFacebookId(facebookUser.facebookId)
            eu should not be (null)

        }
    }
}
