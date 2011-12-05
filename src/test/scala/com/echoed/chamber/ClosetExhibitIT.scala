package com.echoed.chamber

import domain.views.EchoView
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.echoed.chamber.tags.IntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import scala.reflect.BeanProperty
import com.echoed.chamber.dao.{EchoDao, EchoedUserDao}
import com.echoed.chamber.domain.Echo
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import com.echoed.chamber.dao.views.ClosetDao
import org.openqa.selenium.{Cookie, WebDriver}
import java.util.{Date, Properties}
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import java.net.URL
import com.echoed.util.{ScalaObjectMapper, DataCreator}
import org.codehaus.jackson.`type`.TypeReference
import java.util.{List => JList}
import scala.collection.JavaConversions._
import collection.mutable.Buffer


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:apiIT.xml"))
class ClosetExhibitIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = _
    @Autowired @BeanProperty var echoDao: EchoDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    @Autowired @BeanProperty var urls: Properties = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)


    var exhibitUrl: String = _

    {
        exhibitUrl = urls.getProperty("exhibitUrl")
        exhibitUrl != null
    } ensuring (_ == true, "Missing parameters")


    var echoedUser = dataCreator.echoedUser.copy(facebookUserId = null, twitterUserId = null)
    var echoes = dataCreator.echoes

    def cleanup() {
        echoDao.deleteByRetailerId(echoes(0).retailerId)
        echoedUserDao.deleteByEmail(echoedUser.email)
        echoedUserDao.deleteByScreenName(echoedUser.screenName)
    }

    override protected def beforeAll() {
        cleanup
        echoedUserDao.insert(echoedUser)
        dataCreator.echoes.foreach(echoDao.insert(_))
    }

    override protected def afterAll() = cleanup()


    feature("A developer can get closet data as json") {

        info("As a recent developer")
        info("I want to be able to get closet data as json")
        info("by going to echoed.com/closet/exhibit with a valid access token")

        scenario("a request for a user's closet without a valid access token is denied", IntegrationTest) {
            given("a request for a user's closet")
            when("without a valid access token")
            then("deny the request")
            pending
        }

        scenario("a request for a user's closet with a valid access token is granted", IntegrationTest) {

            given("a request for a user's closet")
            when("there is a valid access token") //TODO need to be using an access token!
            then("grant the request")
            and("the returned data can be parsed into a domain object")
            val url = new URL(exhibitUrl + "?echoedUserId=" + echoedUser.id)
            val echoList: Buffer[EchoView] = asScalaBuffer(
                    new ScalaObjectMapper().readValue(url, new TypeReference[JList[EchoView]]() {}))

            for (echo <- echoes) echoList.find(_.echoId == echo.id) should not be (None)
        }

    }

}
