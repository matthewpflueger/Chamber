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
class ChapterDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var chapterDao: ChapterDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val chapter = dataCreator.chapter

    def cleanup() {
        chapterDao.deleteByStoryId(chapter.storyId)
        chapterDao.findByStoryId(chapter.storyId) should have size(0)
    }

    override def beforeAll = cleanup

    override def afterAll = cleanup

    feature("A developer can manipulate Chapter data") {

        scenario("new Chapter is inserted", IntegrationTest) {
            chapterDao.insert(chapter)
            val chapters = chapterDao.findByStoryId(chapter.storyId)
            chapters should have size(1)
            chapter should equal(chapters(0))
        }


    }

}
