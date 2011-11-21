package com.echoed.chamber

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.scalatest.matchers.ShouldMatchers
import com.echoed.chamber.tags.IntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import scala.reflect.BeanProperty
import com.echoed.chamber.dao.{EchoDao, EchoedUserDao}
import com.echoed.chamber.domain.{Echo, EchoedUser}
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import com.echoed.chamber.dao.views.ClosetDao
import org.openqa.selenium.{Cookie, WebDriver}
import java.util.{Date, Properties}


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:itest.xml"))
class ClosetLoginIT extends FeatureSpec with GivenWhenThen with ShouldMatchers {

    @Autowired @BeanProperty var closetDao: ClosetDao = _
    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = _
    @Autowired @BeanProperty var echoDao: EchoDao = _
    @Autowired @BeanProperty var echoHelper: EchoHelper = _
    @Autowired @BeanProperty var webDriver: WebDriver = _

    @Autowired @BeanProperty var urls: Properties = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)


    var closetUrl: String = _

    {
        closetUrl = urls.getProperty("closetUrl")
        closetUrl != null
    } ensuring (_ == true, "Missing parameters")



    feature("A user can view their past echoes by logging into echoed.com via a social platform") {

        info("As a recent purchaser")
        info("I want to be able to view my past purchases")
        info("by going to echoed.com and logging in")

        scenario("an unknown user navigates to echoed.com/closet and is asked to login", IntegrationTest) {
            given("a request to echoed.com/closet")
            when("there is no user information")
            then("redirect to Echoed's login page")
            and("prompt the user to login using a social platform")



        }

        scenario("a known user with no access token navigates to echoed.com/closet and is asked to login", IntegrationTest) {
            given("a request to echoed.com/closet")
            when("there is a known user but no access token")
            then("redirect to Echoed's login page")
            and("prompt the user to login using a social platform")
            pending
        }

        scenario("a known user navigates to echoed.com/closet and is shown their closet", IntegrationTest) {

            val echoedUser = new EchoedUser(null, "matthew.pflueger", "matthew.pflueger@gmail.com", "Matthew", "Pflueger", null, null)
            echoedUserDao.deleteByEmail("matthew.pflueger@gmail.com")
            echoedUserDao.insert(echoedUser)

            val (echoPossibility1, _) = echoHelper.setupEchoPossibility(step = "echoed", echoedUserId = echoedUser.id)
            val echo1 = new Echo(echoPossibility1)
            echoDao.deleteByEchoPossibilityId(echoPossibility1.id)
            echoDao.insert(echo1)

            val (echoPossibility2, _) = echoHelper.setupEchoPossibility(
                step = "echoed", echoedUserId = echoedUser.id, retailerId = "testRetailerId2")
            val echo2 = new Echo(echoPossibility2)
            echoDao.deleteByEchoPossibilityId(echoPossibility2.id)
            echoDao.insert(echo2)

            val closet = closetDao.findByEchoedUserId(echoedUser.id)
            closet should not be null
            closet.echoedUser should not be null
            closet.echoedUser.id should equal (echoedUser.id)

            closet.echoes should not be null
            closet.echoes.size() should equal (2)

            given("a request to echoed.com/closet")
            when("there is a known user")
            val cookie = new Cookie.Builder("echoedUserId", echoedUser.id)
                    .domain(".echoed.com")
                    .path("/")
                    .expiresOn(new Date((new Date().getTime + (1000*60*60*24))))
                    .build()

            webDriver.navigate().to("http://www.echoed.com")
            webDriver.manage().addCookie(cookie)
            webDriver.navigate().to(closetUrl)

            then("show the user their closet")
            webDriver.getTitle should be ("Closet")

            val pageSource = webDriver.getPageSource
            pageSource should include("Matthew Pflueger")
            pageSource should include(echo1.price)
            pageSource should include(echo1.imageUrl)
            pageSource should include(echo2.price)
            pageSource should include(echo2.imageUrl)

            //TODO and("log the activity")
        }

    }
}
