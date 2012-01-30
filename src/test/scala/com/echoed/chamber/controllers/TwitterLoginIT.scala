package com.echoed.chamber.controllers

import com.echoed.chamber.dao.EchoedUserDao
import com.echoed.chamber.dao.TwitterUserDao
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import java.util.Properties
import org.openqa.selenium.{By, WebDriver}
import com.echoed.chamber.util.DataCreator
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import com.echoed.util.{WebDriverUtils, IntegrationTest}


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:webIT.xml"))
class TwitterLoginIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = _
    @Autowired @BeanProperty var twitterUserDao: TwitterUserDao = _
    @Autowired @BeanProperty var echoHelper: EchoHelper = _
    @Autowired @BeanProperty var webDriver: WebDriver = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _
    @Autowired @BeanProperty var urls: Properties = _

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


    val twitterUser = dataCreator.twitterUser

    def cleanup() {
        WebDriverUtils.clearEchoedCookies(webDriver)
        twitterUserDao.deleteByScreenName(twitterUser.screenName)
        echoedUserDao.deleteByScreenName(twitterUser.screenName)
    }

    override protected def beforeAll() = cleanup()

    override protected def afterAll() = cleanup()


    feature("A user can echo and create their Echoed account by login via Twitter") {

        info("As a recent purchaser")
        info("I want to be able to click on the Twitter login button")
        info("So that I can echo and create my Echoed account using my Twitter credentials")

        scenario("unknown user clicks on Twitter login button with a valid echoPossibility and is redirected to confirm page post login", IntegrationTest) {

            given("a request to login and echo using Twitter credentials")
            when("the user is unrecognized (no cookie) and with a valid echoPossibility")

            val (e, count) = echoHelper.setupEchoPossibility(step = "login") //this must match proper step...
            val url = e.asUrlParams(prefix = echoUrl + "?", encode = true)
            webDriver.get(url)
            webDriver.getCurrentUrl should startWith (echoUrl)

            webDriver.findElement(By.id("twitterLogin")).click()
            Thread.sleep(500)
            webDriver.findElement(By.id("username_or_email")).sendKeys(twitterUser.screenName)
            val pass = webDriver.findElement(By.id("password"))
            pass.sendKeys(dataCreator.twitterPassword)
            pass.submit()


            then("redirect to the echo confirm page")
            webDriver.getCurrentUrl.startsWith(echoUrl) should be (true)

            and("create an EchoedUser account using the Twitter info")
            val dbtwitterUser = twitterUserDao.findByScreenName(twitterUser.screenName)
            dbtwitterUser should not be (null)

            val echoedUser = echoedUserDao.findByTwitterUserId(dbtwitterUser.id)
            echoedUser should not be (null)
            val echoPossibility = e.copy(echoedUserId = echoedUser.id, step = "confirm")

            and("record the EchoPossibility in the database")
            echoHelper.validateEchoPossibility(echoPossibility, count)
        }

        scenario("known user clicks on Twitter login button with a valid echoPossibility and is redirected to confirm page post login", IntegrationTest) {

            WebDriverUtils.clearTwitterCookies(webDriver)
            WebDriverUtils.clearEchoedCookies(webDriver)

            given("a request to login and echo using Twitter credentials")
            when("the user is unrecognized (no cookie) and with a valid echoPossibility")

            val (e, count) = echoHelper.setupEchoPossibility(step = "login") //this must match proper step...
            val url = e.asUrlParams(prefix = echoUrl + "?", encode = true)
            webDriver.get(url)
            webDriver.getCurrentUrl should startWith (echoUrl)

            webDriver.findElement(By.id("twitterLogin")).click()
            Thread.sleep(500)
            webDriver.findElement(By.id("username_or_email")).sendKeys(twitterUser.screenName)
            val pass = webDriver.findElement(By.id("password"))
            pass.sendKeys(dataCreator.twitterPassword)
            pass.submit()

            then("redirect to the echo confirm page")
            webDriver.getCurrentUrl.startsWith(echoUrl) should be (true)


            and("create an EchoedUser account using the Twitter info")
            val dbtwitterUser = twitterUserDao.findByScreenName(twitterUser.screenName)
            dbtwitterUser should not be (null)

            val echoedUser = echoedUserDao.findByTwitterUserId(dbtwitterUser.id)
            echoedUser should not be (null)
            val echoPossibility = e.copy(echoedUserId = echoedUser.id, step = "confirm")

            and("record the EchoPossibility in the database")
            echoHelper.validateEchoPossibility(echoPossibility, count)
        }

    }
}
