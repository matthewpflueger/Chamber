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
class FacebookTestUserDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var facebookTestUserDao: FacebookTestUserDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val facebookTestUser = dataCreator.facebookTestUser

    def cleanup() {
        facebookTestUserDao.deleteByEmail(facebookTestUser.email)
        facebookTestUserDao.findByEmail(facebookTestUser.email) should be (null)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate FacebookTestUser data") {

        scenario("a new FacebookTestUser is inserted", IntegrationTest) {
            facebookTestUserDao.insertOrUpdate(facebookTestUser)
            facebookTestUserDao.findById(facebookTestUser.id) should not be (null)
        }


    }

}
