package com.echoed.chamber.controllers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.springframework.beans.factory.annotation.Autowired
import scala.reflect.BeanProperty
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import com.echoed.chamber.dao.views.ClosetDao
import java.util.{Date, Properties}
import com.echoed.chamber.util.DataCreator
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import com.echoed.chamber.dao.{PartnerUserDao, EchoDao, EchoedUserDao}
import com.echoed.util.{WebDriverUtils, IntegrationTest}
import org.openqa.selenium.{Dimension, By, Cookie, WebDriver}
import org.openqa.selenium.firefox.{FirefoxProfile, FirefoxDriver}


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:webIT.xml"))
class PartnerUserLoginIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var partnerUserDao: PartnerUserDao = _
    @Autowired @BeanProperty var webDriver: WebDriver = _
    @Autowired @BeanProperty var webDriverUtils: WebDriverUtils = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    @Autowired @BeanProperty var urls: Properties = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    var dashboardUrl: String = _

    {
        dashboardUrl = urls.getProperty("dashboardUrl")
        dashboardUrl != null
    } ensuring (_ == true, "Missing parameters")


    val partnerUser = dataCreator.partnerUser


    def cleanup() {
        webDriverUtils.clearEchoedCookies()
        partnerUserDao.deleteByEmail(partnerUser.email)
    }

    override def beforeAll = {
        cleanup
        partnerUserDao.insert(partnerUser)
    }

    override def afterAll = cleanup


    feature("A partner user can view their dashboard by logging into Echoed") {

        info("As a partner user")
        info("I want to be able to view my dashboard")
        info("by going to Echoed and logging in")

        scenario("an unknown user navigates directly to their dashboard and is asked to login", IntegrationTest) {
            given("a request to view the partner dashboard")
            when("there is no user information")
            webDriverUtils.clearEchoedCookies()
            webDriver.navigate().to(dashboardUrl)

            then("redirect to Echoed's partner login page")
            and("prompt the user to login")
            webDriver.getTitle should be ("Echoed | Partner Login")
        }

        scenario("a partner user logs in using bad credentials and is shown the login page", IntegrationTest) {
            webDriver.getTitle should be ("Echoed | Partner Login")

            given("a request to login")
            when("there are bad credentials")
            webDriver.findElement(By.id("email")).sendKeys("bademail")
            webDriver.findElement(By.id("password")).sendKeys("badpassword")
            webDriver.findElement(By.id("login")).click()

            then("redirect back to the login page")
            and("prompt the user to try again")
            webDriver.getTitle should be ("Echoed | Partner Login")
            webDriver.getPageSource should include("Error")
            webDriver.manage().getCookieNamed("partnerUser") should be(null)


            webDriver.findElement(By.id("email")).sendKeys(partnerUser.email)
            webDriver.findElement(By.id("password")).sendKeys("badpassword")
            webDriver.findElement(By.id("login")).click()

            webDriver.getTitle should be ("Echoed | Partner Login")
            webDriver.getPageSource should include("Error")
            webDriver.manage().getCookieNamed("partnerUser") should be(null)
        }

        scenario("a partner user logs in using good credentials and is shown the dashboard page", IntegrationTest) {
            webDriver.getTitle should be ("Echoed | Partner Login")

            given("a request to login")
            when("there are good credentials")
            webDriver.findElement(By.id("email")).sendKeys(partnerUser.email)
            webDriver.findElement(By.id("password")).sendKeys(dataCreator.partnerUserPassword)
            webDriver.findElement(By.id("login")).click()

            then("then redirect to the dashboard page")
            and("set an access cookie")
            webDriver.getTitle should startWith ("Echoed")
            webDriver.getPageSource should include(partnerUser.name)
            webDriverUtils.findPartnerUserCookie() should not be(null)
        }

        scenario("a partner user logs out and is redirected out of their dashboard", IntegrationTest) {
            webDriver.getTitle should startWith ("Echoed")

            val dimension = webDriver.manage().window().getSize
            webDriver.manage().window().setSize(new Dimension(1200, dimension.getHeight))

            given("a request to logout")
            when("the user is logged in")
            webDriver.findElement(By.id("logout")).click()

            then("then redirect away from the dashboard")
            and("remove the access cookie")
            webDriver.getPageSource should not include(partnerUser.name)
            webDriverUtils.findPartnerUserCookie() should be(null)
        }

        //This test will succeed on the first, fresh run, but fail (and really should never pass once the partner
        //user logs out) because the first run caches the partner user and subsequent runs the partner user has
        //the same email/password but different ids
//        scenario("a known user navigates directly to their dashboard and is shown their dashboard", IntegrationTest) {
//
//            given("a request to view the partner dashboard")
//            when("there is a known user")
//            val pu = partnerUserDao.findByEmail(partnerUser.email)
//            val cookie = new Cookie.Builder("partnerUser", pu.id)
//                    .domain(".echoed.com")
//                    .path("/")
//                    .expiresOn(new Date((new Date().getTime + (1000*60*60*24))))
//                    .build()
//
//            webDriver.navigate.to("http://www.echoed.com")
//            webDriver.manage.deleteAllCookies
//            webDriver.manage.addCookie(cookie)
//            webDriver.navigate.to(dashboardUrl)
//
//            then("show the user their dashboard")
//            webDriver.getTitle should startWith ("Dashboard")
//
//            val pageSource = webDriver.getPageSource
//            pageSource should include(partnerUser.name)
//        }

    }
}
