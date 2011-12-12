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
class TwitterStatusDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var twitterStatusDao: TwitterStatusDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val twitterStatuses = dataCreator.twitterStatuses

    def cleanup() {
        twitterStatusDao.deleteByEchoedUserId(twitterStatuses(0).echoedUserId)
        twitterStatusDao.findByEchoedUserId(twitterStatuses(0).echoedUserId).size should equal (0)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate TwitterStatus data") {

        scenario("new TwitterStatuses are inserted", IntegrationTest) {
            twitterStatuses.foreach(twitterStatusDao.insert(_))
            val twitterStatusList = asScalaBuffer(twitterStatusDao.findByEchoedUserId(twitterStatuses(0).echoedUserId))
            twitterStatusList should not be (null)
            twitterStatusList.length should equal (twitterStatuses.length)

            for (twitterStatus <- twitterStatuses) twitterStatusList.find(_.id == twitterStatus.id) should not be (None)
        }


    }

}
