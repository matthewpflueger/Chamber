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
class FacebookUserDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var facebookUserDao: FacebookUserDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val facebookUser = dataCreator.facebookUser

    def cleanup() {
        facebookUserDao.deleteByEmail(facebookUser.email)
        facebookUserDao.findByEmail(facebookUser.email) should be (null)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate FacebookUser data") {

        scenario("a new FacebookUser is inserted", IntegrationTest) {
            facebookUserDao.insertOrUpdate(facebookUser)
            facebookUserDao.findById(facebookUser.id) should not be (null)
        }


    }

}
