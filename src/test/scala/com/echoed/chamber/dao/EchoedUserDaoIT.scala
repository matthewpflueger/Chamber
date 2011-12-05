package com.echoed.chamber.dao

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}

import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.tags.IntegrationTest
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import com.echoed.util.DataCreator


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:databaseIT.xml"))
class EchoedUserDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val echoedUser = dataCreator.echoedUser

    def cleanup() {
        echoedUserDao.deleteByEmail(echoedUser.email)
        echoedUserDao.deleteByScreenName(echoedUser.screenName)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate EchoedUser data") {

        scenario("a new EchoedUser is inserted", IntegrationTest) {
            echoedUserDao.insert(echoedUser)
            echoedUserDao.findById(echoedUser.id) should not be (null)
        }

        scenario("an EchoedUser is found by facebookUserId", IntegrationTest) {
            echoedUserDao.findByFacebookUserId(echoedUser.facebookUserId) should not be (null)
        }

        scenario("an EchoedUser is found by twitterUserId", IntegrationTest) {
            echoedUserDao.findByTwitterUserId(echoedUser.twitterUserId) should not be (null)
        }

    }

}
