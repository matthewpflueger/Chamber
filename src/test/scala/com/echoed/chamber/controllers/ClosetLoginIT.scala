package com.echoed.chamber.controllers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.springframework.beans.factory.annotation.Autowired
import scala.reflect.BeanProperty
import com.echoed.chamber.domain.{Echo, EchoedUser}
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import com.echoed.chamber.dao.views.ClosetDao
import org.openqa.selenium.{Cookie, WebDriver}
import java.util.{Date, Properties}
import com.echoed.chamber.util.DataCreator
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import com.echoed.util.IntegrationTest
import com.echoed.util.WebDriverUtils
import com.echoed.chamber.dao.{EchoMetricsDao, FacebookUserDao, EchoDao, EchoedUserDao}


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:webIT.xml"))
class ClosetLoginIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var closetDao: ClosetDao = _
    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = _
    @Autowired @BeanProperty var facebookUserDao: FacebookUserDao = _
    @Autowired @BeanProperty var echoDao: EchoDao = _
    @Autowired @BeanProperty var echoMetricsDao: EchoMetricsDao = _
    @Autowired @BeanProperty var echoHelper: EchoHelper = _
    @Autowired @BeanProperty var webDriver: WebDriver = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _
    @Autowired @BeanProperty var webDriverUtils: WebDriverUtils = _

    @Autowired @BeanProperty var urls: Properties = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    var closetUrl: String = _

    {
        closetUrl = urls.getProperty("closetUrl")
        closetUrl != null
    } ensuring (_ == true, "Missing parameters")


    val echoedUser = dataCreator.echoedUser.copy(twitterUserId = null)
    val facebookUser = dataCreator.facebookUser
    val echoes = dataCreator.echoes
    val echoMetrics = dataCreator.echoMetrics


    def cleanup() {
        echoedUserDao.deleteByEmail(echoedUser.email)
        echoedUserDao.deleteByScreenName(echoedUser.screenName)
        facebookUserDao.deleteByEmail(facebookUser.email)
        echoDao.deleteByRetailerId(echoes(0).retailerId)
        echoDao.findByRetailerId(echoes(0).retailerId).size should equal (0)
        echoMetricsDao.deleteByRetailerId(echoes(0).retailerId)
        echoMetricsDao.findByRetailerId(echoes(0).retailerId).size should equal (0)
    }

    override def beforeAll = {
        facebookUser.echoedUserId should equal(echoedUser.id)
        cleanup
        echoedUserDao.insert(echoedUser)
        facebookUserDao.insertOrUpdate(facebookUser)

    }

    override def afterAll = cleanup


    feature("A user can view their past echoes by logging into Echoed via a social platform") {

        info("As a recent purchaser")
        info("I want to be able to view my past purchases")
        info("by going to site url and logging in")

        scenario("an unknown user navigates to exhibit url and is asked to login", IntegrationTest) {
            given("a request to view an exhibit")
            when("there is no user information")
            then("redirect to Echoed's login page")
            and("prompt the user to login using a social platform")
            pending
        }

        scenario("a known user with no access token navigates to exhibit url and is asked to login", IntegrationTest) {
            given("a request to exhibit")
            when("there is a known user but no access token")
            then("redirect to Echoed's login page")
            and("prompt the user to login using a social platform")
            pending
        }

        scenario("a known user with no echoes navigates to exhibit url and is shown their empty closet", IntegrationTest) {
            given("a request to see their exhibit")
            when("there is a known user with no echoes")
            then("then show the user their exhibit")
            and("and indicate they have no echoes")
            val pageSource = webDriverUtils.navigateToCloset(echoedUser)
            pageSource should include("Total Savings: $0.00")
        }

        scenario("a known user navigates to exhibit and is shown their closet", IntegrationTest) {
            echoes.foreach(echoDao.insert(_))
            echoes.foreach(echoDao.updateForEcho(_))

            val closet = closetDao.findByEchoedUserId(echoedUser.id)
            closet should not be null
            closet.echoedUser should not be null
            closet.echoedUser.id should equal (echoedUser.id)

            closet.echoes should not be null
            closet.echoes.size() should equal (echoes.length)

            given("a request to the exhibit url")
            when("there is a known user")
            then("show the user their exhibit")
            val pageSource = webDriverUtils.navigateToCloset(echoedUser)
            pageSource should include(echoMetrics.map(_.credit).sum.round.toString)
            //TODO potential for pre-caching but until then...  echoes.foreach(echo => pageSource should include(echo.imageUrl))
        }

    }
}
