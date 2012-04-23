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
class PartnerUserDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var partnerUserDao: PartnerUserDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val partnerUser = dataCreator.partnerUser

    def cleanup() {
        partnerUserDao.deleteByEmail(partnerUser.email)
        partnerUserDao.findByEmail(partnerUser.email) should be (null)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate PartnerUser data") {

        scenario("a new PartnerUser is inserted", IntegrationTest) {
            partnerUserDao.insert(partnerUser)
            partnerUserDao.findById(partnerUser.id) should not be (null)
        }

        scenario("a PartnerUser can be found by email", IntegrationTest) {
            val ru = partnerUserDao.findByEmail(partnerUser.email)
            ru should not be (null)
            ru.password should equal (partnerUser.password)
            ru.isPassword(dataCreator.partnerUserPassword) should be (true)
        }

    }

}