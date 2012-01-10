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
import java.util.{Calendar, Date}



@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:databaseIT.xml"))
class RetailerSettingsDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var retailerSettingsDao: RetailerSettingsDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val retailerSettings = dataCreator.retailerSettings
    val retailerSettingsFuture = dataCreator.retailerSettingsFuture
    val retailerSettingsList = dataCreator.retailerSettingsList
    val retailerId = retailerSettings.retailerId


    def cleanup() {
        retailerSettingsList.foreach(rs => retailerSettingsDao.deleteByRetailerId(rs.retailerId))
        retailerSettingsDao.findByRetailerId(retailerSettings.retailerId).size should equal (0)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate RetailerSettings data") {

        scenario("new RetailerSettings are inserted", IntegrationTest) {
            retailerSettingsList.foreach(retailerSettingsDao.insert(_))
            val rsList = asScalaBuffer(retailerSettingsDao.findByRetailerId(retailerSettings.retailerId))
            rsList should not be (null)
            rsList.length should equal (2)

            for (retailerSetting <- rsList) rsList.find(_.id == retailerSetting.id) should not be (None)
        }

        scenario("only one active RetailerSettings for a given date", IntegrationTest) {
            val currentSettings = retailerSettingsDao.findByActiveOn(retailerId, new Date)
            currentSettings should not be (null)
            currentSettings.id should equal (retailerSettings.id)

            val future = dataCreator.future
            future.set(Calendar.YEAR, dataCreator.future.get(Calendar.YEAR) + 1)
            val futureSettings = retailerSettingsDao.findByActiveOn(retailerId, future.getTime)
            futureSettings should not be (null)
            futureSettings.id should equal (retailerSettingsFuture.id)

            val past = dataCreator.past
            past.set(Calendar.YEAR, dataCreator.past.get(Calendar.YEAR) - 1)
            val noSettings = retailerSettingsDao.findByActiveOn(retailerId, past.getTime)
            noSettings should be (null)
        }

    }

}
