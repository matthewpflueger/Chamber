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
class EchoPossibilityDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var echoPossibilityDao: EchoPossibilityDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val echoPossibilities = dataCreator.echoPossibilities

    def cleanup() {
        echoPossibilityDao.deleteByRetailerId(echoPossibilities(0).retailerId)
        echoPossibilityDao.findByRetailerId(echoPossibilities(0).retailerId).size should equal (0)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate EchoPossibility data") {

        scenario("new EchoPossibities are inserted", IntegrationTest) {
            echoPossibilities.foreach(echoPossibilityDao.insertOrUpdate(_))
            val echoPossibilityList = asScalaBuffer(echoPossibilityDao.findByRetailerId(echoPossibilities(0).retailerId))
            echoPossibilityList should not be (null)
            echoPossibilityList.length should equal (echoPossibilities.length)

            for (echoPossibility <- echoPossibilities) echoPossibilityList.find(_.id == echoPossibility.id) should not be (None)
        }


    }

}
