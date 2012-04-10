package com.echoed.chamber.services.facebook

import org.slf4j.LoggerFactory
import scala.reflect.BeanProperty
import scalaz._
import Scalaz._
import com.echoed.chamber.services.ActorClient
import java.util.{Calendar, Date}
import com.echoed.chamber.dao.{FacebookCommentDao, FacebookLikeDao, FacebookPostDao}
import akka.actor.{Scheduler, Actor}
import java.util.concurrent.{Future, TimeUnit}
import akka.dispatch.CompletableFuture
import com.echoed.chamber.domain.FacebookPost


class FacebookPostCrawlerActor extends Actor {


    private val logger = LoggerFactory.getLogger(classOf[FacebookPostCrawlerActor])

    @BeanProperty var facebookPostDao: FacebookPostDao = _
    @BeanProperty var facebookLikeDao: FacebookLikeDao = _
    @BeanProperty var facebookCommentDao: FacebookCommentDao = _
    @BeanProperty var facebookAccess: ActorClient = _
    @BeanProperty var interval: Long = 60000
    @BeanProperty var future: Option[CompletableFuture[GetPostDataResponse]] = None
    @BeanProperty var postedOnDaysBefore: Int = -8
    @BeanProperty var postedOnHoursBefore: Int = -1

    private var scheduledMessage: Option[Future[AnyRef]] = None

    override def preStart() {
        next
    }

    def next(response: GetPostDataResponse) {
        future.foreach(_.completeWithResult(response))
        next
    }

    def next {
        //interval less than one is used for testing - we will handle the message sending in a test
        if (interval > 0) self ! 'next
    }

    def findFacebookPostToCrawl = {
        val postedOnStartDate = {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_MONTH, postedOnDaysBefore)
            cal.getTime
        }

        val postedOnEndDate = {
            val cal = Calendar.getInstance()
            cal.add(Calendar.HOUR, postedOnHoursBefore)
            cal.getTime
        }

        val crawledOnEndDate = postedOnEndDate

        Option(facebookPostDao.findPostToCrawl(postedOnStartDate, postedOnEndDate, crawledOnEndDate, null)).getOrElse {
            Option(facebookPostDao.findOldPostToCrawl(postedOnStartDate, postedOnStartDate, null)).orNull
        }
    }

    protected def updateForCrawl(
            msg: GetPostDataResponse,
            facebookPost: FacebookPost,
            crawlStatus: String,
            retries: Int)(f: Unit => Unit) {
        try {
            logger.debug("Updating post %s for crawl status %s, retries %s" format (facebookPost.id, crawlStatus, retries))
            scheduledMessage.foreach(_.cancel(false))
            facebookPostDao.updatePostForCrawl(facebookPost.copy(crawledStatus = crawlStatus, crawledOn = new Date, retries = retries))
            f()
        } finally {
            next(msg)
        }
    }

    protected def receive = {
        case 'next =>
            try {
                Option(findFacebookPostToCrawl).cata({ facebookPostToCrawl =>
                    logger.debug("Found for crawling {}", facebookPostToCrawl)
                    facebookAccess.actorRef ! GetPostData(facebookPostToCrawl)
                    facebookPostDao.updatePostForCrawl(facebookPostToCrawl.facebookPost.copy(
                            crawledStatus = "started", crawledOn = new Date))
                },
                { logger.debug("No posts found for crawling") })
            } finally {
                //always make sure we are looking for posts to crawl (less than one interval for testing)
                if (interval > 0) scheduledMessage = Option(Scheduler.scheduleOnce(self, 'next, interval, TimeUnit.MILLISECONDS))
            }


        case msg @ GetPostDataResponse(GetPostData(facebookPostToCrawl), Right(facebookPostData)) =>
            updateForCrawl(msg, facebookPostToCrawl.facebookPost, "crawled", 0) { _ =>
                facebookPostData.likes.foreach { facebookLikeDao.insertOrUpdate(_) }
                facebookPostData.comments.foreach { facebookCommentDao.insertOrUpdate(_) }
                logger.debug("Received good response for FacebookPost {}", facebookPostData.facebookPost.id)
            }

        case msg @ GetPostDataResponse(_, Left(GetPostDataOAuthError(facebookPost, _, _, message))) =>
            //don't bother retrying this crawl
            updateForCrawl(msg, facebookPost, message, retries = 1000) { _ =>
                logger.debug("Received auth error crawling FacebookPost {}", facebookPost.id)
            }

        case msg @ GetPostDataResponse(_, Left(GetPostDataFalse(_, facebookPost))) =>
            //don't bother retrying this crawl
            updateForCrawl(msg, facebookPost, "false", retries = 1000) { _ =>
                logger.debug("Received false response crawling FacebookPost {}", facebookPost.id)
            }

        case msg @ GetPostDataResponse(GetPostData(facebookPostToCrawl), Left(e @ GetPostDataError(_, _, _, _))) => //facebookPost, t, c, m))) =>
            val message = "%s, type %s, code %s" format(e.m, e.`type`, e.code)
            updateForCrawl(msg, e.facebookPost, message, retries = facebookPostToCrawl.facebookPost.retries + 1) { _ =>
                logger.debug("Received error response {} crawling FacebookPost {}", message, e.facebookPost.id)
            }

        case msg @ GetPostDataResponse(GetPostData(facebookPostToCrawl), Left(e)) =>
            updateForCrawl(msg, facebookPostToCrawl.facebookPost, e.message.take(254), facebookPostToCrawl.facebookPost.retries + 1) { _ =>
                logger.debug("Received bad response crawling FacebookPost {}: {}", facebookPostToCrawl.id, e.message)
            }
    }
}

