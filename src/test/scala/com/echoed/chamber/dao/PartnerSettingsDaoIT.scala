package com.echoed.chamber.dao

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import partner.PartnerSettingsDao
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}

import com.echoed.util.IntegrationTest
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import com.echoed.chamber.util.DataCreator
import scala.collection.JavaConversions._
import java.util.{Calendar, Date}



@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:databaseIT.xml"))
class PartnerSettingsDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var partnerSettingsDao: PartnerSettingsDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val partnerSettings = dataCreator.partnerSettings
    val partnerSettingsFuture = dataCreator.partnerSettingsFuture
    val partnerSettingsList = dataCreator.partnerSettingsList
    val partnerId = partnerSettings.partnerId


    def cleanup() {
        partnerSettingsList.foreach(rs => partnerSettingsDao.deleteByPartnerId(rs.partnerId))
        partnerSettingsDao.findByPartnerId(partnerSettings.partnerId).size should equal (0)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate PartnerSettings data") {

        scenario("new PartnerSettings are inserted", IntegrationTest) {
            partnerSettingsList.foreach(partnerSettingsDao.insert(_))
            val rsList = asScalaBuffer(partnerSettingsDao.findByPartnerId(partnerSettings.partnerId))
            rsList should not be (null)
            rsList.length should equal (2)

            for (partnerSetting <- rsList) rsList.find(_.id == partnerSetting.id) should not be (None)
        }

        scenario("only one active PartnerSettings for a given date", IntegrationTest) {
            val currentSettings = partnerSettingsDao.findByActiveOn(partnerId, new Date)
            currentSettings should not be (null)
            currentSettings.id should equal (partnerSettings.id)

            val future = dataCreator.future
            future.set(Calendar.YEAR, dataCreator.future.get(Calendar.YEAR) + 1)
            val futureSettings = partnerSettingsDao.findByActiveOn(partnerId, future.getTime)
            futureSettings should not be (null)
            futureSettings.id should equal (partnerSettingsFuture.id)

            val past = dataCreator.past
            past.set(Calendar.YEAR, dataCreator.past.get(Calendar.YEAR) - 1)
            val noSettings = partnerSettingsDao.findByActiveOn(partnerId, past.getTime)
            noSettings should be (null)
        }

    }

}
