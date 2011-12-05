package com.echoed.chamber.dao

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}

import com.echoed.chamber.tags.IntegrationTest
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import com.echoed.util.DataCreator
import scala.collection.JavaConversions._


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:databaseIT.xml"))
class FacebookFriendDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var facebookFriendDao: FacebookFriendDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val facebookFriends = dataCreator.facebookFriends

    def cleanup() {
        facebookFriendDao.deleteByFacebookUserId(facebookFriends(0).facebookUserId)
        facebookFriendDao.findByFacebookUserId(facebookFriends(0).facebookUserId).size should equal (0)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate FacebookFriend data") {

        scenario("new FacebookFriends are inserted", IntegrationTest) {
            facebookFriends.foreach(facebookFriendDao.insert(_))
            val facebookFriendList = asScalaBuffer(facebookFriendDao.findByFacebookUserId(facebookFriends(0).facebookUserId))
            facebookFriendList should not be (null)
            facebookFriendList.length should equal (facebookFriends.length)

            for (facebookFriend <- facebookFriends) facebookFriendList.find(_.id == facebookFriend.id) should not be (None)
        }


    }

}
