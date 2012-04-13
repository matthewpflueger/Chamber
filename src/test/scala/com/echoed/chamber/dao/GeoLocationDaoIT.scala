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
import org.slf4j.LoggerFactory
import java.util.{Date, Calendar}


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:databaseIT.xml"))
class GeoLocationDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    private val logger = LoggerFactory.getLogger(classOf[GeoLocationDaoIT])

    @Autowired @BeanProperty var echoDao: EchoDao = _
    @Autowired @BeanProperty var echoClickDao: EchoClickDao = _
    @Autowired @BeanProperty var geoLocationDao: GeoLocationDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val echoes = dataCreator.echoes
    val echoClicks_1 = dataCreator.echoClicks_1
    val echoClicks_2 = dataCreator.echoClicks_2
    val geoLocations = dataCreator.geoLocations

    def cleanup() {
        echoes.foreach(e => echoDao.deleteById(e.id))
        echoClicks_1.foreach(ec => echoClickDao.deleteByEchoId(ec.echoId))
        geoLocations.foreach(gl => geoLocationDao.deleteByIpAddress(gl.ipAddress))
    }

    override def beforeAll = {
        cleanup
    }

    override def afterAll = cleanup


    feature("A developer can manipulate GeoLocation data") {

        scenario("GeoLocations are found for crawling from Echo", IntegrationTest) {

            var time = new Date()

            echoes.foreach { e =>
                echoDao.insert(e.copy(createdOn = time))
                time = new Date(time.getTime + 1000)
            }

            val date = new Date
            var geoLocation = geoLocationDao.findForCrawl(date, false)
            geoLocation.id should be (null)
            geoLocation.ipAddress should equal(echoes(1).ipAddress) //newest first

            geoLocationDao.insertOrUpdate(geoLocations(1)) //geoLocations.filter(_.ipAddress == echoes(1).ipAddress)(0))

            geoLocation = geoLocationDao.findForCrawl(date, false)
            geoLocation.id should be (null)
            geoLocation.ipAddress should equal(echoes(0).ipAddress)
            geoLocationDao.insertOrUpdate(geoLocations(0)) //geoLocations.filter(_.ipAddress == echoes(0).ipAddress)(0))

            geoLocation = geoLocationDao.findForCrawl(date, false)
            if (geoLocation != null) {
                geoLocations.foreach(_.ipAddress should not equal(geoLocation.ipAddress))
            }
        }

        scenario("GeoLocations are found for crawling from EchoClick", IntegrationTest) {

            var time = new Date()

            echoClicks_1.foreach { e =>
                echoClickDao.insert(e.copy(createdOn = time))
                time = new Date(time.getTime + 1000)
            }
            echoClicks_2.foreach { e =>
                echoClickDao.insert(e.copy(createdOn = time))
                time = new Date(time.getTime + 1000)
            }

            val date = new Date
            var geoLocation = geoLocationDao.findForCrawl(date, true)
            geoLocation.id should be (null)
            geoLocation.ipAddress should equal(echoClicks_2(1).ipAddress) //newest first

            geoLocationDao.insertOrUpdate(geoLocations(3)) //geoLocations.filter(_.ipAddress == echoes(1).ipAddress)(0))

            geoLocation = geoLocationDao.findForCrawl(date, true)
            geoLocation.id should be (null)
            geoLocation.ipAddress should equal(echoClicks_1(1).ipAddress)
            geoLocationDao.insertOrUpdate(geoLocations(2)) //geoLocations.filter(_.ipAddress == echoes(0).ipAddress)(0))

            geoLocation = geoLocationDao.findForCrawl(date, true)
            if (geoLocation != null) {
                geoLocations.foreach(_.ipAddress should not equal(geoLocation.ipAddress))
            }
        }

    }

}
