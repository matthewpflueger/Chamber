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
class FacebookLikeDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var facebookLikeDao: FacebookLikeDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val facebookLikes = dataCreator.facebookLikes

    def cleanup() {
        facebookLikeDao.deleteByFacebookPostId(facebookLikes(0).facebookPostId)
        facebookLikeDao.findByFacebookPostId(facebookLikes(0).facebookPostId).size should equal (0)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate FacebookLike data") {

        scenario("new FacebookLikes are inserted", IntegrationTest) {
            facebookLikes.foreach(facebookLikeDao.insertOrUpdate(_))
            val facebookLikeList = asScalaBuffer(facebookLikeDao.findByFacebookPostId(facebookLikes(0).facebookPostId))
            facebookLikeList should not be (null)
            facebookLikeList.length should equal (facebookLikes.length)

            for (facebookLike <- facebookLikes) facebookLikeList.find(_.id == facebookLike.id) should not be (None)
        }


    }

}
