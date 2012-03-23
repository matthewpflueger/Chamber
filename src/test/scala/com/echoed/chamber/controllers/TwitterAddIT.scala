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
import com.echoed.util.WebDriverUtils


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:webIT.xml"))
class TwitterAddIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = _
    @Autowired @BeanProperty var facebookUserDao: FacebookUserDao = _
    @Autowired @BeanProperty var twitterUserDao: TwitterUserDao = _
    @Autowired @BeanProperty var echoHelper: EchoHelper = _
    @Autowired @BeanProperty var webDriver: WebDriver = _
    @Autowired @BeanProperty var webDriverUtils: WebDriverUtils = _
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
        twitterId = null)
    val twitterUser = dataCreator.twitterUser.copy(id = UUID.randomUUID.toString)


    def cleanup() {
        twitterUserDao.deleteByScreenName(twitterUser.screenName)
        echoedUserDao.deleteByEmail(echoedUser.email)
        echoedUserDao.deleteByScreenName(echoedUser.screenName)
        webDriverUtils.clearTwitterCookies()
        webDriverUtils.clearEchoedCookies(true)
    }

    override protected def beforeAll() {
        cleanup
        echoedUserDao.insert(echoedUser)
    }

    override protected def afterAll() = cleanup()

    var firstScenarioPassed = false

    feature("A user can add their Twitter account to their existing Echoed user account") {

        info("As an Echoed user")
        info("I want to be able to click on the add Twitter account button")
        info("So that I can add my Twitter account to my Echoed account")

        scenario("user clicks on add Twitter account button and successfully adds their Twitter account to their Echoed account", IntegrationTest) {

            given("a request to add their Twitter account to their Echoed account")
            webDriverUtils.navigateToCloset(echoedUser)

            when("the user has no associated Twitter account and their Twitter credentials matches no existing Twitter account")
            then("send them to Twitter to login")
            webDriver.findElement(By.id("addTwitterLink")).click
            Thread.sleep(1000)
            webDriver.findElement(By.id("username_or_email")).sendKeys(twitterUser.screenName)
            val pass = webDriver.findElement(By.id("password"))
            pass.sendKeys(dataCreator.twitterPassword)
            pass.submit()


            and("add their Twitter account to their Echoed user account")
            Thread.sleep(1000)
            webDriver.getTitle() should startWith ("Echoed")
            webDriver.findElement(By.id("twitterAccount")) should not be (null)
            val eu = echoedUserDao.findByTwitterId(twitterUser.twitterId)
            eu should not be (null)
            firstScenarioPassed = true
        }


        scenario("user clicks on add Twitter account button and tries to add existing Twitter account to Echoed user account", IntegrationTest) {
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


                given("a request to add their Twitter account to their Echoed account")

                webDriverUtils.clearTwitterCookies()
                webDriverUtils.clearEchoedCookies()

                webDriverUtils.navigateToCloset(echoedUser2)

                when("the user has no associated Twitter account and their Twitter credentials matches an existing Twitter account")
                then("send them to Twitter to login")
                webDriver.findElement(By.id("addTwitterLink")).click
                //NOTE: this does not work because we can't seem to get the Twitter cookies to clear
                //webDriver.getTitle should include("Twitter")
                Thread.sleep(1000)
                webDriver.findElement(By.id("username_or_email")).sendKeys(twitterUser.screenName)
                val pass = webDriver.findElement(By.id("password"))
                pass.sendKeys(dataCreator.twitterPassword)
                pass.submit()


                and("do not add the Twitter account")
                Thread.sleep(1000)
                webDriver.getTitle should be ("Error")
//                evaluating { webDriver.findElement(By.id("twitterAccount")) } should produce [org.openqa.selenium.NoSuchElementException]
//                webDriver.findElement(By.id("addTwitterLink")) should not be(null)

                and("show them an error explanation")
                webDriver.getPageSource should include ("Twitter account already in use")
            } finally {
                echoedUserDao.deleteByEmail(echoedUser2.email)
            }
        }

        /* Because of the MyBatis cache this test does not work because we modify the database behind the scenes - need to flush the cache! */
        scenario("user tries to add their own, already existing Twitter account to Echoed user account", IntegrationTest) {
            firstScenarioPassed should be (true)

            webDriverUtils.clearTwitterCookies()
            webDriverUtils.clearEchoedCookies(true)

            echoedUserDao.unlinkTwitter(echoedUser)

            webDriverUtils.navigateToCloset(echoedUser)

            given("a request to add their own, already existing Twitter account to their Echoed account")
            webDriver.getTitle should startWith("Echoed")

            when("the user has an associated Twitter account")
            then("send them to Twitter to login")
            Thread.sleep(1000)
            webDriver.findElement(By.id("addTwitterLink")).click
            Thread.sleep(1000)
            webDriver.findElement(By.id("username_or_email")).sendKeys(twitterUser.screenName)
            val pass = webDriver.findElement(By.id("password"))
            pass.sendKeys(dataCreator.twitterPassword)
            pass.submit()


            and("re-assign their Twitter account to their Echoed user account")
            Thread.sleep(1000)
            webDriver.getTitle() should startWith ("Echoed")
            webDriver.findElement(By.id("twitterAccount")) should not be (null)
            val eu = echoedUserDao.findByTwitterId(twitterUser.twitterId)
            eu should not be (null)

        }
    }
}
