package com.echoed.chamber.dao.views

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
import com.echoed.chamber.dao.{EchoMetricsDao, EchoDao, EchoedUserDao}


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:databaseIT.xml"))
class ClosetDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var closetDao: ClosetDao = _
    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = _
    @Autowired @BeanProperty var echoDao: EchoDao = _
    @Autowired @BeanProperty var echoMetricsDao: EchoMetricsDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val echoedUser = dataCreator.echoedUser
    val echoes = dataCreator.echoes
    val echoMetrics = dataCreator.echoMetrics
    val partnerSettings = dataCreator.partnerSettings

    def cleanup() {
        echoedUserDao.deleteByEmail(echoedUser.email)
        echoedUserDao.deleteByScreenName(echoedUser.screenName)
        echoDao.deleteByPartnerId(echoes(0).partnerId)
        echoDao.findByPartnerId(echoes(0).partnerId).size should equal (0)
        echoMetricsDao.deleteByPartnerId(echoes(0).partnerId)
        echoMetricsDao.findByPartnerId(echoes(0).partnerId).size should equal (0)
    }

    override def beforeAll = {
        cleanup
        echoedUserDao.insert(echoedUser)
        echoes.foreach { echo =>
            echoDao.insert(echo)
            echoDao.updateForEcho(echo)
        }
        echoMetrics.map(_.echoed(partnerSettings).clicked(partnerSettings).clicked(partnerSettings)).foreach(echoMetricsDao.insert(_))
    }

    override def afterAll = cleanup

    feature("A developer can view Closet data") {

        scenario("Closet data is queried", IntegrationTest) {
            val closet = closetDao.findByEchoedUserId(echoedUser.id, 0 , 30)
            closet should not be(null)
            closet.echoedUser should not be(null)
            closet.echoes should not be(null)
            closet.echoedUser.id should equal(echoedUser.id)

            val echoList = asScalaBuffer(closet.echoes)
            echoList should not be (null)
            echoList.length should equal (echoes.length)


            for (echo <- echoes) echoList.find(_.echoId == echo.id) should not be (None)
        }

        scenario("Sum of echo credit is queried", IntegrationTest) {
            val totalCredit = closetDao.totalCreditByEchoedUserId(echoedUser.id)
            totalCredit should not be(0f)

            echoMetricsDao.findByPartnerId(echoes(0).partnerId).map(_.credit).sum should be(totalCredit)
        }

        scenario("Sum of echo clicks is queried", IntegrationTest) {
            val totalClicks = closetDao.totalClicksByEchoedUserId(echoedUser.id)
            totalClicks should not be(0)
            echoMetricsDao.findByEchoedUserId(echoes(0).echoedUserId).map(_.totalClicks).sum should be (totalClicks)
        }

    }

}
