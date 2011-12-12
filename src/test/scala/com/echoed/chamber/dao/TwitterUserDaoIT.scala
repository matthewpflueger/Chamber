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


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:databaseIT.xml"))
class TwitterUserDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var twitterUserDao: TwitterUserDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val twitterUser = dataCreator.twitterUser

    def cleanup() {
        twitterUserDao.deleteById(twitterUser.id)
        twitterUserDao.deleteByScreenName(twitterUser.screenName)
        twitterUserDao.findById(twitterUser.id) should be (null)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate TwitterUser data") {

        scenario("a new TwitterUser is inserted", IntegrationTest) {
            twitterUserDao.insert(twitterUser)
            twitterUserDao.findById(twitterUser.id) should not be (null)
        }

    }

}
