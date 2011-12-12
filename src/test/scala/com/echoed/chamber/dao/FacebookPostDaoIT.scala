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
class FacebookPostDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var facebookPostDao: FacebookPostDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val facebookPosts = dataCreator.facebookPosts

    def cleanup() {
        facebookPostDao.deleteByEchoedUserId(facebookPosts(0).echoedUserId)
        facebookPostDao.findByEchoedUserId(facebookPosts(0).echoedUserId).size should equal (0)
    }

    override def beforeAll = cleanup
    override def afterAll = cleanup

    feature("A developer can manipulate FacebookPost data") {

        scenario("new FacebookPosts are inserted", IntegrationTest) {
            facebookPosts.foreach(facebookPostDao.insert(_))
            val facebookPostList = asScalaBuffer(facebookPostDao.findByEchoedUserId(facebookPosts(0).echoedUserId))
            facebookPostList should not be (null)
            facebookPostList.length should equal (facebookPosts.length)

            for (facebookPost <- facebookPosts) facebookPostList.find(_.id == facebookPost.id) should not be (None)
        }


    }

}
