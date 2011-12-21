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
class FacebookCommentDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var facebookCommentDao: FacebookCommentDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val facebookComments = dataCreator.facebookComments

    def cleanup() {
        facebookCommentDao.deleteByFacebookPostId(facebookComments(0).facebookPostId)
        facebookCommentDao.findByFacebookPostId(facebookComments(0).facebookPostId).size should equal (0)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate FacebookLike data") {

        scenario("new FacebookLikes are inserted", IntegrationTest) {
            facebookComments.foreach(facebookCommentDao.insertOrUpdate(_))
            val facebookLikeList = asScalaBuffer(facebookCommentDao.findByFacebookPostId(facebookComments(0).facebookPostId))
            facebookLikeList should not be (null)
            facebookLikeList.length should equal (facebookComments.length)

            for (facebookLike <- facebookComments) facebookLikeList.find(_.id == facebookLike.id) should not be (None)
        }


    }

}
