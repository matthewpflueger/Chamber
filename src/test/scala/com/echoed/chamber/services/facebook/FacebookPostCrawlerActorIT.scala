package com.echoed.chamber.services.facebook

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
import java.util.Properties
import java.util.Date
import com.echoed.util.IntegrationTest
import scala.collection.JavaConversions
import org.openqa.selenium.{By, Cookie, WebDriver}
import com.echoed.chamber.domain.{Echo, FacebookPost}
import com.echoed.chamber.dao._
import com.echoed.chamber.util.DataCreator
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, FeatureSpec}
import com.echoed.chamber.controllers.EchoHelper
import akka.testkit.TestActorRef.apply
import akka.testkit.TestActorRef
import com.echoed.chamber.services.ActorClient


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:apiIT.xml"))
class FacebookPostCrawlerActorIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    @Autowired @BeanProperty var echoDao: EchoDao = _
    @Autowired @BeanProperty var echoClickDao: EchoClickDao = _
    @Autowired @BeanProperty var facebookUserDao: FacebookUserDao = _
    @Autowired @BeanProperty var facebookLikeDao: FacebookLikeDao = _
    @Autowired @BeanProperty var facebookCommentDao: FacebookCommentDao = _
    @Autowired @BeanProperty var facebookPostDao: FacebookPostDao = _
    @Autowired @BeanProperty var echoedUserDao: EchoedUserDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    @Autowired @BeanProperty var facebookAccessActorProperties: Properties = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)


    val facebookPosts = dataCreator.facebookPosts
    val facebookUser = dataCreator.facebookUser

    def cleanup() {
        facebookPostDao.deleteByEchoedUserId(facebookPosts(0).echoedUserId)
        facebookPostDao.findByEchoedUserId(facebookPosts(0).echoedUserId).size should equal (0)
        facebookUserDao.deleteByEmail(facebookUser.email)
    }

    override def beforeAll = {
        facebookAccessActorProperties should not be(null)
        facebookUser.id should equal(facebookPosts(0).facebookUserId)
        cleanup
        facebookUserDao.insertOrUpdate(facebookUser.copy(id = "facebookUserId")) //TODO do not set the facebookUserId
        //insert a bunch of facebook posts
    }

    //override def afterAll = cleanup


    feature("Likes and comments of a post will be tracked") {

        info("As a purchaser or retailer")
        info("I want to know how many people have liked or commented on a echoed purchase to Facebook")
        info("So that I can track how influential my echoed purchases are")

        scenario("a echoed purchase that has been posted to Facebook will be crawled for likes and comments", IntegrationTest) {
            given("a echoed purchase")
            when("it has been posted to Facebook and not crawled")
            then("crawl the post")
            and("save the likes and comments in the database")

            val facebookAccessActor = TestActorRef[FacebookAccessActor]
            facebookAccessActor.underlyingActor.properties = facebookAccessActorProperties
            val facebookPostCrawlerActor = TestActorRef[FacebookPostCrawlerActor]
            facebookPostCrawlerActor.underlyingActor.facebookAccess = new ActorClient {
                var actorRef = facebookAccessActor
            }
            facebookPostCrawlerActor.underlyingActor.facebookCommentDao = facebookCommentDao
            facebookPostCrawlerActor.underlyingActor.facebookLikeDao = facebookLikeDao
            facebookPostCrawlerActor.underlyingActor.facebookPostDao = facebookPostDao
            facebookPostCrawlerActor.underlyingActor.interval = -1


            facebookAccessActor.start
            facebookPostCrawlerActor.start

            facebookPostCrawlerActor ! 'next
            Thread.sleep(600000)
        }
    }
}
