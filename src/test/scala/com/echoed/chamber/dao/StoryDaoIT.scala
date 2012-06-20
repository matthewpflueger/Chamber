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
class StoryDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var storyDao: StoryDao = _
    @Autowired @BeanProperty var imageDao: ImageDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val story = dataCreator.story

    def cleanup() {
        imageDao.deleteByUrl(story.image.url)
        storyDao.deleteByEchoedUserId(story.echoedUserId)
        storyDao.findByEchoedUserId(story.echoedUserId) should have size(0)
    }

    override def beforeAll = cleanup

    override def afterAll = cleanup

    feature("A developer can manipulate Story data") {

        scenario("new Story is inserted", IntegrationTest) {
            imageDao.insert(story.image)
            storyDao.insert(story)
            val stories = storyDao.findByEchoedUserId(story.echoedUserId)
            stories should have size(1)
            story.copy(image = null) should equal(stories(0).copy(image = null))
        }


    }

}
