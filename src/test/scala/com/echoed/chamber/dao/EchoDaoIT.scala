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
import scala.collection.JavaConversions._


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:databaseIT.xml"))
class EchoDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var echoDao: EchoDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val echoes = dataCreator.echoes

    def cleanup() {
        echoDao.deleteByRetailerId(echoes(0).retailerId)
        echoDao.findByRetailerId(echoes(0).retailerId).size should equal (0)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate Echo data") {

        scenario("new Echoes are inserted", IntegrationTest) {
            echoes.foreach(echoDao.insert(_))
            val echoList = asScalaBuffer(echoDao.findByRetailerId(echoes(0).retailerId))
            echoList should not be (null)
            echoList.length should equal (echoes.length)

            for (echo <- echoes) echoList.find(el => {
                if (el.id == echo.id) {
                    //el.fee should equal (echo.fee)
                    //el.credit should equal (echo.credit)
                    true
                } else false
            }) should not be (None)
        }


    }

}
