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
import com.echoed.chamber.dao.{AdminUserDao, EchoDao, EchoedUserDao}
import org.openqa.selenium.{By, Cookie, WebDriver}
import com.echoed.util.{WebDriverUtils, IntegrationTest}


/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/15/12
 * Time: 10:55 AM
 * To change this template use File | Settings | File Templates.
 */

@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:webIT.xml"))
class AdminUserLoginIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var adminUserDao: AdminUserDao = _
    @Autowired @BeanProperty var webDriver: WebDriver = _
    @Autowired @BeanProperty var webDriverUtils: WebDriverUtils = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    @Autowired @BeanProperty var urls: Properties = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    var adminDashboardUrl: String = _

    {
        adminDashboardUrl = urls.getProperty("adminDashboardUrl")
        adminDashboardUrl != null
    } ensuring (_ == true, "Missing parameters")
    
    val adminUser = dataCreator.adminUser
    
    def cleanup(){
        webDriverUtils.clearEchoedCookies()
        adminUserDao.deleteByEmail(adminUser.email)
    }
    
    override def beforeAll = {
        cleanup()
        adminUserDao.insert(adminUser)
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
            webDriver.navigate().to(adminDashboardUrl)

            then("redirect to Echoed's partner login page")
            and("prompt the user to login")
            webDriver.getTitle should be ("Echoed | Admin Login")
        }

        scenario("An admin user logs in using bad credentials and is shown the login page", IntegrationTest) {
            webDriver.getTitle should be ("Echoed | Partner Login")

            given("a request to login")
            when("there are bad credentials")
            webDriver.findElement(By.id("email")).sendKeys("bademail")
            webDriver.findElement(By.id("password")).sendKeys("badpassword")
            webDriver.findElement(By.id("login")).click()

            then("redirect back to the login page")
            and("prompt the user to try again")
            webDriver.getTitle should be ("Echoed | Admin Login")
            webDriver.getPageSource should include("Error")
            //webDriver.manage().getCookieNamed("partnerUser") should be(null)

            webDriver.findElement(By.id("email")).sendKeys(adminUser.email)
            webDriver.findElement(By.id("password")).sendKeys("badpassword")
            webDriver.findElement(By.id("login")).click()

            webDriver.getTitle should be ("Echoed | Admin Login")
            webDriver.getPageSource should include("Error")
            webDriverUtils.findAdminUserCookie() should be(null)

        }

        scenario("An Admin user logs in using good credentials and is shown the dashboard page", IntegrationTest) {
            webDriver.getTitle should be ("Echoed | Partner Login")

            given("a request to login")
            when("there are good credentials")
            webDriver.findElement(By.id("email")).sendKeys(adminUser.email)
            webDriver.findElement(By.id("password")).sendKeys(dataCreator.adminUserPassword)
            webDriver.findElement(By.id("login")).click()

            then("then redirect to the dashboard page")
            and("set an access cookie")
            webDriver.getTitle should startWith ("Echoed")
            webDriver.getPageSource should include(adminUser.name)
            webDriverUtils.findAdminUserCookie() should not be(null)
        }

        scenario("An Admin user logs out and is redirected out of their dashboard", IntegrationTest) {
            webDriver.getTitle should startWith ("Echoed")

            given("a request to logout")
            when("the user is logged in")
            webDriver.findElement(By.id("logout")).click()

            then("then redirect away from the dashboard")
            and("remove the access cookie")
            webDriver.getPageSource should not include(adminUser.name)
            webDriverUtils.findAdminUserCookie() should be(null)
        }



    }

}
