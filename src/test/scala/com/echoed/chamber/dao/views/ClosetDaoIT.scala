package com.echoed.chamber.dao.views

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
import com.echoed.chamber.dao.{EchoDao, EchoedUserDao}


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:databaseIT.xml"))
class ClosetDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var closetDao: ClosetDao = _
    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = _
    @Autowired @BeanProperty var echoDao: EchoDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val echoedUser = dataCreator.echoedUser
    val echoes = dataCreator.echoes

    def cleanup() {
        echoedUserDao.deleteByEmail(echoedUser.email)
        echoedUserDao.deleteByScreenName(echoedUser.screenName)
        echoDao.deleteByRetailerId(echoes(0).retailerId)
        echoDao.findByRetailerId(echoes(0).retailerId).size should equal (0)
    }

    override def beforeAll = {
        cleanup
        echoedUserDao.insert(echoedUser)
        echoes.foreach(echoDao.insert(_))
    }
    override def afterAll = cleanup

    feature("A developer can view Closet data") {

        scenario("Closet data is queried", IntegrationTest) {
            val closet = closetDao.findByEchoedUserId(echoedUser.id)
            closet should not be(null)
            closet.echoedUser should not be(null)
            closet.echoes should not be(null)
            closet.echoedUser.id should equal(echoedUser.id)

            val echoList = asScalaBuffer(closet.echoes) //echoDao.findByRetailerId(echoes(0).retailerId))
            echoList should not be (null)
            echoList.length should equal (echoes.length)

            for (echo <- echoes) echoList.find(_.id == echo.id) should not be (None)
        }


    }

}
