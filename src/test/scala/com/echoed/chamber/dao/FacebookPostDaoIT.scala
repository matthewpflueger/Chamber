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
import com.echoed.chamber.domain.FacebookPost
import org.slf4j.LoggerFactory
import java.util.{Date, Calendar}


@RunWith(classOf[JUnitRunner])
@ContextConfiguration(locations = Array("classpath:databaseIT.xml"))
class FacebookPostDaoIT extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

    private val logger = LoggerFactory.getLogger(classOf[FacebookPostDaoIT])

    @Autowired @BeanProperty var facebookPostDao: FacebookPostDao = _
    @Autowired @BeanProperty var facebookUserDao: FacebookUserDao = _
    @Autowired @BeanProperty var dataCreator: DataCreator = _

    new TestContextManager(this.getClass()).prepareTestInstance(this)

    val facebookPosts = dataCreator.facebookPosts
    val facebookUser = dataCreator.facebookUser

    def cleanup() {
        facebookPostDao.deleteByEchoedUserId(facebookPosts(0).echoedUserId)
        facebookPostDao.findByEchoedUserId(facebookPosts(0).echoedUserId).size should equal (0)
        facebookUserDao.deleteByEmail(facebookUser.email)
    }

    override def beforeAll = {
        facebookUser.id should equal(facebookPosts(0).facebookUserId)
        cleanup
        facebookUserDao.insertOrUpdate(facebookUser)
    }

    override def afterAll = cleanup

    val postedOnStartCalendar = {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -8)
        cal
    }

    val postedOnEndCalendar = {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -1)
        cal
    }

    val crawledOnEndCalendar = {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -1)
        cal
    }

    feature("A developer can manipulate FacebookPost data") {

        scenario("new FacebookPosts are inserted", IntegrationTest) {
            facebookPosts.foreach(facebookPostDao.insert(_))
            val facebookPostList = asScalaBuffer(facebookPostDao.findByEchoedUserId(facebookPosts(0).echoedUserId))
            facebookPostList should not be (null)
            facebookPostList.length should equal (facebookPosts.length)

            for (facebookPost <- facebookPosts) facebookPostList.find(_.id == facebookPost.id) should not be (None)
        }

        scenario("very new FacebookPosts will not be found for crawling") {
            given("a bunch of Facebook posts that were successfully posted less than 24 hours ago")
            when("looking for posts to crawl")
            val facebookPost = facebookPostDao.findPostToCrawl(
                postedOnStartCalendar.getTime,
                postedOnEndCalendar.getTime,
                crawledOnEndCalendar.getTime,
                facebookPosts(0).echoedUserId)
            then("none of these posts should be returned")
            facebookPost should be (null)
        }

        scenario("new FacebookPosts will be found for crawling") {
            given("a bunch of Facebook posts that were successfully posted 24 hours ago and never crawled")
            when("looking for posts to crawl")
            then("return these posts for crawling")

            val facebookPost = facebookPostDao.findPostToCrawl(
                postedOnStartCalendar.getTime,
                new Date,
                crawledOnEndCalendar.getTime,
                facebookPosts(0).echoedUserId)
            facebookPost should not be (null)
            facebookPosts.find(_.id == facebookPost.id) should not be (null)
        }

        scenario("FacebookPosts that were not posted will not be found for crawling") {
            given("a bunch of Facebook posts that were unsuccessfully posted to Facebook")
            when("looking for posts to crawl")
            then("these posts should not be returned")
            facebookPostDao.deleteByEchoedUserId(facebookPosts(0).echoedUserId)
            facebookPosts.foreach { fp =>
                facebookPostDao.insert(fp.copy(postedOn = null, facebookId = null))
            }
            val facebookPost = facebookPostDao.findPostToCrawl(
                postedOnStartCalendar.getTime,
                new Date,
                crawledOnEndCalendar.getTime,
                facebookPosts(0).echoedUserId)
            facebookPost should be (null)
        }

        scenario("old FacebookPosts will be found for crawling") {
            given("a bunch of Facebook posts that were successfully posted more than a week ago and not crawled within 24 hours")
            when("looking for old posts to crawl")
            then("return these posts for crawling")

            val oldPostedOn = {
                val cal = Calendar.getInstance
                cal.add(Calendar.DAY_OF_MONTH, -10)
                cal.getTime
            }

            facebookPostDao.deleteByEchoedUserId(facebookPosts(0).echoedUserId)
            facebookPosts.foreach { fp =>
                facebookPostDao.insert(fp.copy(postedOn = oldPostedOn, crawledOn = oldPostedOn))
            }

            val facebookPost = facebookPostDao.findOldPostToCrawl(
                postedOnStartCalendar.getTime,
                postedOnStartCalendar.getTime,
                facebookPosts(0).echoedUserId)

            facebookPost should not be (null)
            facebookPosts.find(_.id == facebookPost.id) should not be (null)
        }
    }

}
