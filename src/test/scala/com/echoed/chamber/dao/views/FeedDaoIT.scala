package com.echoed.chamber.dao.views

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
import com.echoed.chamber.dao._


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:databaseIT.xml"))
class FeedDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var feedDao: FeedDao = _
    @Autowired @BeanProperty var storyDao: StoryDao = _
    @Autowired @BeanProperty var chapterDao: ChapterDao = _
    @Autowired @BeanProperty var imageDao: ImageDao = _
    @Autowired @BeanProperty var chapterImageDao: ChapterImageDao = _
    @Autowired @BeanProperty var commentDao: CommentDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val echoedUser = dataCreator.echoedUser
    val story = dataCreator.story
    val chapter = dataCreator.chapter
    val chapterImage = dataCreator.chapterImage
    val comment = dataCreator.comment

    def cleanup() {
        commentDao.deleteById(comment.id)
        imageDao.deleteByUrl(chapterImage.image.url)
        chapterImageDao.deleteById(chapterImage.id)
        chapterDao.deleteByStoryId(story.id)
        storyDao.deleteByEchoedUserId(echoedUser.id)
    }

    override def beforeAll = {
        cleanup
        storyDao.insert(story)
        chapterDao.insert(chapter)
        imageDao.insert(chapterImage.image)
        chapterImageDao.insert(chapterImage)
        commentDao.insert(comment)
    }

    override def afterAll = cleanup

    feature("A developer can view Story data") {

        scenario("Story data is queried", IntegrationTest) {
            val storyFull = feedDao.getStory(story.id)
            storyFull.story should equal(story)
            storyFull.chapters(0) should equal(chapter)
            storyFull.chapterImages(0).copy(image = null) should equal(chapterImage.copy(image = null))
            storyFull.comments(0) should equal(comment)
        }
    }

}
