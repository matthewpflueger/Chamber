package com.echoed.chamber.dao

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


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:databaseIT.xml"))
class EchoClickDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var echoClickDao: EchoClickDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val echoClicks_1 = dataCreator.echoClicks_1
    val echoedUser = dataCreator.echoedUser

    def cleanup() {
        echoClickDao.deleteByEchoId(echoClicks_1(0).echoId)
        echoClickDao.findByEchoId(echoClicks_1(0).echoId).size should equal (0)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate EchoClick data") {

        scenario("new EchoClicks are inserted", IntegrationTest) {
            echoClicks_1.foreach(echoClickDao.insert(_))
            val echoClicks = asScalaBuffer(echoClickDao.findByEchoId(echoClicks_1(0).echoId))
            echoClicks should not be (null)
            echoClicks.length should equal (echoClicks_1.length)

            for (echoClick <- echoClicks_1) echoClicks.find(_.id == echoClick.id) should not be (None)
        }


    }

}
