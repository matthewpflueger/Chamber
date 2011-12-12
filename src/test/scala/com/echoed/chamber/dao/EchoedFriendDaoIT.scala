package com.echoed.chamber.dao

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}

import com.echoed.util.IntegrationTest
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import com.echoed.chamber.util.DataCreator
import scala.collection.JavaConversions._


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:databaseIT.xml"))
class EchoedFriendDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var echoedFriendDao: EchoedFriendDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val echoedFriends = dataCreator.echoedFriends

    def cleanup() {
        echoedFriendDao.deleteByEchoedUserId(echoedFriends(0).fromEchoedUserId)
        echoedFriendDao.deleteByEchoedUserId(echoedFriends(0).toEchoedUserId)
        echoedFriendDao.findByEchoedUserId(echoedFriends(0).fromEchoedUserId).size should equal (0)
        echoedFriendDao.findByEchoedUserId(echoedFriends(0).toEchoedUserId).size should equal (0)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate FacebookFriend data") {

        scenario("new FacebookFriends are inserted", IntegrationTest) {
            echoedFriends.foreach(echoedFriendDao.insertOrUpdate(_))
            val echoedFriendList = asScalaBuffer(echoedFriendDao.findByEchoedUserId(echoedFriends(0).fromEchoedUserId))
            echoedFriendList should not be (null)
            echoedFriendList.length should equal (1)
            echoedFriendList(0).fromEchoedUserId should equal(echoedFriends(0).fromEchoedUserId)


            val toEchoedFriendList = asScalaBuffer(echoedFriendDao.findByEchoedUserId(echoedFriends(0).toEchoedUserId))
            toEchoedFriendList should not be (null)
            toEchoedFriendList.length should equal (1)
            toEchoedFriendList(0).fromEchoedUserId should equal(echoedFriends(0).toEchoedUserId)
        }


    }

}
