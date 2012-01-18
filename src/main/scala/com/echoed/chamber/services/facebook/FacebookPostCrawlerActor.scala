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


class FacebookPostCrawlerActor extends Actor {


    private val logger = LoggerFactory.getLogger(classOf[FacebookPostCrawlerActor])

    @BeanProperty var facebookPostDao: FacebookPostDao = _
    @BeanProperty var facebookLikeDao: FacebookLikeDao = _
    @BeanProperty var facebookCommentDao: FacebookCommentDao = _
    @BeanProperty var facebookAccess: ActorClient = _
    @BeanProperty var interval: Long = 60000
    @BeanProperty var future: Option[CompletableFuture[GetPostDataResponse]] = None

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
            cal.add(Calendar.DAY_OF_MONTH, -8)
            cal.getTime
        }

        val postedOnEndDate = {
            val cal = Calendar.getInstance()
            cal.add(Calendar.HOUR, -1)
            cal.getTime
        }

        val crawledOnEndDate = postedOnEndDate

        Option(facebookPostDao.findPostToCrawl(postedOnStartDate, postedOnEndDate, crawledOnEndDate)).getOrElse {
            Option(facebookPostDao.findOldPostToCrawl(postedOnStartDate, postedOnStartDate)).orNull
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
            try {
                scheduledMessage.foreach(_.cancel(false))
                logger.debug("Received good response crawling FacebookPost {}", facebookPostToCrawl.id)
                facebookPostDao.updatePostForCrawl(facebookPostToCrawl.facebookPost.copy(
                        crawledStatus = "crawled", crawledOn = new Date))
                facebookPostData.likes.foreach { facebookLikeDao.insertOrUpdate(_) }
                facebookPostData.comments.foreach { facebookCommentDao.insertOrUpdate(_) }
            } finally {
                next(msg)
            }

        case msg @ GetPostDataResponse(_, Left(GetPostDataFalse(_, facebookPost))) =>
            try {
                scheduledMessage.foreach(_.cancel(false))
                logger.debug("Received false response crawling FacebookPost {}", facebookPost.id)
                facebookPostDao.updatePostForCrawl(facebookPost.copy(crawledStatus = "false", crawledOn = new Date))
            } finally {
                next(msg)
            }

        case msg @ GetPostDataResponse(GetPostData(facebookPostToCrawl), Left(e)) =>
            try {
                scheduledMessage.foreach(_.cancel(false))
                logger.debug("Received bad response crawling FacebookPost {}: {}", facebookPostToCrawl.id, e.message)
                facebookPostDao.updatePostForCrawl(facebookPostToCrawl.facebookPost.copy(
                    crawledStatus = e.message, crawledOn = new Date))
            } finally {
                next(msg)
            }
    }
}

