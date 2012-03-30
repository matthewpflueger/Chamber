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
class ImageDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var imageDao: ImageDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val images = dataCreator.images

    def cleanup() {
        images.foreach(i => imageDao.deleteByUrl(i.url))
        images.foreach(i => imageDao.findByUrl(i.url) should be(null))
    }

    override def beforeAll = cleanup

    override def afterAll = cleanup

    feature("A developer can manipulate Image data") {

        scenario("new Images are inserted", IntegrationTest) {
            images.foreach(imageDao.insert(_))
            images.foreach(i => imageDao.findByUrl(i.url) should not be(null))
        }


    }

}
