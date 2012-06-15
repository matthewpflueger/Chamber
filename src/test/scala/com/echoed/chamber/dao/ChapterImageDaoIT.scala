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
class ChapterImageDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var imageDao: ImageDao = _
    @Autowired @BeanProperty var chapterImageDao: ChapterImageDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val chapterImage = dataCreator.chapterImage

    def cleanup() {
        imageDao.deleteByUrl(dataCreator.chapterImage.image.url)
        imageDao.findByUrl(dataCreator.chapterImage.image.url) should be(null)
        chapterImageDao.deleteById(chapterImage.id)
        chapterImageDao.findByChapterId(chapterImage.id) should have size(0)
    }

    override def beforeAll = cleanup

    override def afterAll = cleanup

    feature("A developer can manipulate ChapterImage data") {

        scenario("new ChapterImage is inserted", IntegrationTest) {
            imageDao.insert(chapterImage.image)
            chapterImageDao.insert(chapterImage)
            val chapterImages = chapterImageDao.findByChapterId(chapterImage.chapterId)
            chapterImages should have size(1)
            chapterImage.copy(image = null) should equal(chapterImages(0).copy(image = null))
        }


    }

}
