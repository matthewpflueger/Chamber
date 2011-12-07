package com.echoed.chamber.dao

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}

import com.echoed.util.IntegrationTest
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import com.echoed.util.DataCreator
import scala.collection.JavaConversions._


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:databaseIT.xml"))
class TwitterFollowerDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var twitterFollowerDao: TwitterFollowerDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val twitterFollowers = dataCreator.twitterFollowers

    def cleanup() {
        twitterFollowerDao.deleteByTwitterUserId(twitterFollowers(0).twitterUserId)
        twitterFollowerDao.findByTwitterUserId(twitterFollowers(0).twitterUserId).size should equal (0)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate TwitterFollower data") {

        scenario("new TwitterFollowers are inserted", IntegrationTest) {
            twitterFollowers.foreach(twitterFollowerDao.insert(_))
            val twitterFollowerList = asScalaBuffer(twitterFollowerDao.findByTwitterUserId(twitterFollowers(0).twitterUserId))
            twitterFollowerList should not be (null)
            twitterFollowerList.length should equal (twitterFollowers.length)

            for (twitterFollower <- twitterFollowers) twitterFollowerList.find(_.id == twitterFollower.id) should not be (None)
        }


    }

}
