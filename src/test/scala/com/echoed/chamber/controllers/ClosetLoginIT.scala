package com.echoed.chamber.controllers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.echoed.util.IntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import scala.reflect.BeanProperty
import com.echoed.chamber.domain.{Echo, EchoedUser}
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import com.echoed.chamber.dao.views.ClosetDao
import org.openqa.selenium.{Cookie, WebDriver}
import java.util.{Date, Properties}
import com.echoed.chamber.util.DataCreator
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import com.echoed.chamber.dao.{FacebookUserDao, EchoDao, EchoedUserDao}


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:webIT.xml"))
class ClosetLoginIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var closetDao: ClosetDao = _
    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = _
    @Autowired @BeanProperty var facebookUserDao: FacebookUserDao = _
    @Autowired @BeanProperty var echoDao: EchoDao = _
    @Autowired @BeanProperty var echoHelper: EchoHelper = _
    @Autowired @BeanProperty var webDriver: WebDriver = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    @Autowired @BeanProperty var urls: Properties = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    var closetUrl: String = _

    {
        closetUrl = urls.getProperty("closetUrl")
        closetUrl != null
    } ensuring (_ == true, "Missing parameters")


    val echoedUser = dataCreator.echoedUser
    val facebookUser = dataCreator.facebookUser
    val echoes = dataCreator.echoes


    def cleanup() {
        echoedUserDao.deleteByEmail(echoedUser.email)
        echoedUserDao.deleteByScreenName(echoedUser.screenName)
        facebookUserDao.deleteByEmail(facebookUser.email)
        echoDao.deleteByRetailerId(echoes(0).retailerId)
        echoDao.findByRetailerId(echoes(0).retailerId).size should equal (0)
    }

    override def beforeAll = {
        facebookUser.echoedUserId should equal(echoedUser.id)
        cleanup
        echoedUserDao.insert(echoedUser)
        facebookUserDao.insertOrUpdate(facebookUser)
        echoes.foreach(echoDao.insert(_))
    }

    override def afterAll = cleanup


    feature("A user can view their past echoes by logging into echoed.com via a social platform") {

        info("As a recent purchaser")
        info("I want to be able to view my past purchases")
        info("by going to echoed.com and logging in")

        scenario("an unknown user navigates to echoed.com/closet and is asked to login", IntegrationTest) {
            given("a request to echoed.com/closet")
            when("there is no user information")
            then("redirect to Echoed's login page")
            and("prompt the user to login using a social platform")
            pending
        }

        scenario("a known user with no access token navigates to echoed.com/closet and is asked to login", IntegrationTest) {
            given("a request to echoed.com/closet")
            when("there is a known user but no access token")
            then("redirect to Echoed's login page")
            and("prompt the user to login using a social platform")
            pending
        }

        scenario("a known user navigates to echoed.com/closet and is shown their closet", IntegrationTest) {

            val closet = closetDao.findByEchoedUserId(echoedUser.id)
            closet should not be null
            closet.echoedUser should not be null
            closet.echoedUser.id should equal (echoedUser.id)

            closet.echoes should not be null
            closet.echoes.size() should equal (echoes.length)

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
            pageSource should include(echoedUser.name)
            pageSource should include(echoes.map(_.credit).sum.round.toString)
            //TODO potential for pre-caching but until then...  echoes.foreach(echo => pageSource should include(echo.imageUrl))
        }

    }
}
