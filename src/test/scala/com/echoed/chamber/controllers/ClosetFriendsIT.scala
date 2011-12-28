package com.echoed.chamber.controllers

import com.echoed.chamber.domain.views.EchoView
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.echoed.util.IntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import scala.reflect.BeanProperty
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import java.net.URL
import com.echoed.util.ScalaObjectMapper
import com.echoed.chamber.util.DataCreator
import org.codehaus.jackson.`type`.TypeReference
import scala.collection.JavaConversions._
import collection.mutable.Buffer
import org.slf4j.LoggerFactory
import com.echoed.chamber.dao.{FacebookUserDao, EchoedFriendDao, EchoDao, EchoedUserDao}
import com.echoed.chamber.domain.{FacebookUser, EchoedUser, EchoedFriend}
import java.util.{UUID, Properties, List => JList}


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:apiIT.xml"))
class ClosetFriendsIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    private val logger = LoggerFactory.getLogger(classOf[ClosetFriendsIT])

    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = _
    @Autowired @BeanProperty var echoedFriendDao: EchoedFriendDao = _
    @Autowired @BeanProperty var facebookUserDao: FacebookUserDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    @Autowired @BeanProperty var urls: Properties = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)


    var friendsUrl: String = _

    {
        friendsUrl = urls.getProperty("friendsUrl")
        friendsUrl != null
    } ensuring (_ == true, "Missing parameters")


    var facebookUser = dataCreator.facebookUser
    var echoedUser = dataCreator.echoedUser.copy(id = UUID.randomUUID.toString, twitterUserId = null)
    var echoedUsers = Buffer.empty[(FacebookUser, EchoedUser)]

    def cleanup() {
        echoedUserDao.deleteByEmail(echoedUser.email)
        echoedUserDao.deleteByScreenName(echoedUser.screenName)
        echoedFriendDao.deleteByEchoedUserId(echoedUser.id)
        facebookUserDao.deleteByEmail(facebookUser.email)
        dataCreator.cleanupEchoedUsers(echoedUsers)
    }

    override protected def beforeAll() {
        cleanup()
        echoedUsers = dataCreator.generateEchoedUsers
        facebookUserDao.insertOrUpdate(facebookUser)
        echoedUserDao.insert(echoedUser)
    }

    override protected def afterAll() = cleanup()


    feature("A developer can get friend data as json") {

        info("As a recent developer")
        info("I want to be able to get friend data as json")
        info("by going to echoed.com/closet/friends with a valid access token")

        scenario("a request for a user's friends without a valid access token is denied", IntegrationTest) {
            given("a request for a user's friends")
            when("without a valid access token")
            then("deny the request")
            pending
        }

        scenario("a request for a user's friends with a valid access token is granted", IntegrationTest) {

            given("a request for a user's friends")
            when("there is a valid access token") //TODO need to be using an access token!
            then("grant the request")
            and("the returned data can be parsed into a domain object")


            val url = new URL(friendsUrl + "?echoedUserId=" + echoedUser.id)
            val objectMapper = new ScalaObjectMapper()


            def fetchFriendList = asScalaBuffer(objectMapper.readValue(url, new TypeReference[JList[EchoedFriend]]() {}))

            logger.debug("Requesting friends at {}", url)
            var friendList = fetchFriendList

            //initial hit should return nothing as we are fetching friends in the background...
            friendList should be ('empty)

            //TODO nasty hack to see if we eventually get friends - keep hitting the url waiting until we get something
            val sleep = (2 to 7).iterator
            while (friendList.isEmpty && sleep.hasNext) {
                Thread.sleep(sleep.next * 1000)
                friendList = fetchFriendList
            }

            logger.debug("Found \n{}", friendList.mkString("\n"))
            friendList should not be('empty)
        }

    }

}
