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


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:databaseIT.xml"))
class RetailerUserDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var retailerUserDao: RetailerUserDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val retailerUser = dataCreator.retailerUser

    def cleanup() {
        retailerUserDao.deleteById(retailerUser.id)
        retailerUserDao.findById(retailerUser.id) should be (null)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate RetailerUser data") {

        scenario("a new RetailerUser is inserted", IntegrationTest) {
            retailerUserDao.insert(retailerUser)
            retailerUserDao.findById(retailerUser.id) should not be (null)
        }

        scenario("a RetailerUser can be found by email", IntegrationTest) {
            val ru = retailerUserDao.findByEmail(retailerUser.email)
            ru should not be (null)
            ru.password should equal (retailerUser.password)
            ru.isPassword(dataCreator.retailerUserPassword) should be (true)
        }

    }

}
