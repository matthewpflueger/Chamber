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
class CommentDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var commentDao: CommentDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val comment = dataCreator.comment

    def cleanup() {
        commentDao.deleteById(comment.id)
        commentDao.findByStoryId(comment.storyId) should have size(0)
    }

    override def beforeAll = cleanup

    override def afterAll = cleanup

    feature("A developer can manipulate Comment data") {

        scenario("new Comment is inserted", IntegrationTest) {
            commentDao.insert(comment)
            val comments = commentDao.findByStoryId(comment.storyId)
            comments should have size(1)
            comment should equal(comments(0))
        }


    }

}
