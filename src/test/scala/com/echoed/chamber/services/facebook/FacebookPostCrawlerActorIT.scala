package com.echoed.chamber.services.facebook

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import reflect.BeanProperty
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.{TestContextManager, ContextConfiguration}
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
import akka.dispatch.DefaultCompletableFuture
import scala.Either
import java.util.{UUID, Calendar, Properties, Date}
import akka.util.Duration


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

    @Autowired @BeanProperty var facebookAccessProperties: Properties = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)


    var facebookPosts = dataCreator.facebookPosts
    val facebookUser = dataCreator.facebookUser

    var falseFacebookPost: FacebookPost = null

    val facebookAccessActor = TestActorRef[FacebookAccessActor]
    val facebookPostCrawlerActor = TestActorRef[FacebookPostCrawlerActor]

    def cleanup() {
        facebookPostDao.deleteByEchoedUserId(facebookPosts(0).echoedUserId)
        facebookPostDao.findByEchoedUserId(facebookPosts(0).echoedUserId).size should equal (0)
        facebookUserDao.deleteByEmail(facebookUser.email)
        facebookPosts.foreach { fp =>
            facebookLikeDao.deleteByFacebookPostId(fp.id)
            facebookCommentDao.deleteByFacebookPostId(fp.id)
        }
    }

    override def beforeAll = {
        facebookAccessProperties should not be(null)
        facebookUser.id should equal(facebookPosts(0).facebookUserId)
        cleanup

        facebookUserDao.insertOrUpdate(facebookUser)

        facebookPosts = facebookPosts.zipWithIndex.map { tuple =>
            val (fp, index) = tuple
            val po = {
                val cal = Calendar.getInstance()
                cal.add(Calendar.HOUR, ((index + 1) * -1) + -1) //convert index to negative and add it minimum age for crawling
                cal.getTime
            }

            val facebookPost = fp.copy(postedOn = po, crawledOn = null)
            facebookPostDao.insert(facebookPost)
            facebookPost
        }

        falseFacebookPost = facebookPosts(0).copy(
            id = UUID.randomUUID.toString,
            facebookId = "100003326847181_117086678412226",
            echoId = UUID.randomUUID.toString)

        facebookAccessActor.underlyingActor.properties = facebookAccessProperties
        facebookPostCrawlerActor.underlyingActor.facebookAccess = new ActorClient {
            var actorRef = facebookAccessActor
        }
        facebookPostCrawlerActor.underlyingActor.facebookCommentDao = facebookCommentDao
        facebookPostCrawlerActor.underlyingActor.facebookLikeDao = facebookLikeDao
        facebookPostCrawlerActor.underlyingActor.facebookPostDao = facebookPostDao
        facebookPostCrawlerActor.underlyingActor.interval = -1

        facebookAccessActor.start
        facebookPostCrawlerActor.start

        facebookAccessActor.isRunning should be(true)
        facebookPostCrawlerActor.isRunning should be(true)
    }

    override def afterAll = cleanup


    feature("Likes and comments of a post will be tracked") {

        info("As a purchaser or retailer")
        info("I want to know how many people have liked or commented on a echoed purchase to Facebook")
        info("So that I can track how influential my echoed purchases are")

        scenario("a echoed purchase that has been posted to Facebook will be crawled for likes and comments", IntegrationTest) {
            given("a echoed purchase")
            when("it has been posted to Facebook and not crawled")
            then("crawl the post")
            and("save the likes and comments in the database")

            facebookPosts.foreach { fp =>
                val future = new DefaultCompletableFuture[GetPostDataResponse]()
                facebookPostCrawlerActor.underlyingActor.future = Some(future)
                facebookPostCrawlerActor ! 'next

                val response = future.await(Duration(10, "seconds")).get.resultOrException
                val likes = facebookLikeDao.findByFacebookPostId(response.facebookPost.id)
                val comments = facebookCommentDao.findByFacebookPostId(response.facebookPost.id)

                likes should not be(null)
                likes should have length(response.likes.length)

                comments should not be(null)
                comments should have length(response.comments.length)
            }
        }

        scenario("a echoed purchase that has been posted to Facebook cannot be crawled for likes and comments", IntegrationTest) {
            given("a echoed purchase")
            when("it has been posted to Facebook and not crawled but will return false upon crawl")
            then("crawl the post")
            and("record the status as false in the database")

            facebookPostDao.insert(falseFacebookPost)
            val future = new DefaultCompletableFuture[GetPostDataResponse]()
            facebookPostCrawlerActor.underlyingActor.future = Some(future)
            facebookPostCrawlerActor ! 'next

            evaluating { future.await(Duration(10, "seconds")).get.resultOrException } should produce [GetPostDataFalse]

            val likes = facebookLikeDao.findByFacebookPostId(falseFacebookPost.id)
            val comments = facebookCommentDao.findByFacebookPostId(falseFacebookPost.id)

            likes should not be(null)
            likes should have length(0)

            comments should not be(null)
            comments should have length(0)

            val fp = facebookPostDao.findByEchoId(falseFacebookPost.echoId)
            fp should not be(null)
            fp.crawledStatus should be("false")
        }

    }
}
