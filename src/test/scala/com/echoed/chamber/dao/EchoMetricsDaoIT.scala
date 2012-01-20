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
class EchoMetricsDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var echoDao: EchoDao = _
    @Autowired @BeanProperty var echoMetricsDao: EchoMetricsDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val echoes = dataCreator.echoes
    val echoMetrics = dataCreator.echoMetrics

    def cleanup() {
        echoMetricsDao.deleteByRetailerId(echoMetrics(0).retailerId)
        echoDao.findByRetailerId(echoes(0).retailerId).size should equal (0)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate EchoMetrics data") {

        scenario("new EchoMetrics are inserted", IntegrationTest) {
            echoMetrics.foreach(echoMetricsDao.insert(_))
            val echoMetricsList = asScalaBuffer(echoMetricsDao.findByRetailerId(echoMetrics(0).retailerId))
            echoMetricsList should not be (null)
            echoMetricsList.length should equal (echoes.length)

            for (echoMetric <- echoMetrics) echoMetricsList.find(el => {
                if (el.id == echoMetric.id) {
                    //el.fee should equal (echo.fee)
                    //el.credit should equal (echo.credit)
                    true
                } else false
            }) should not be (None)
        }


    }

}
