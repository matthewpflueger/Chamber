package com.echoed.chamber.services.facebook

import com.echoed.chamber.services.EchoedService
import com.echoed.chamber.domain.FacebookPost
import akka.actor._


class FacebookPostCrawler(
        facebookAccessCreator: ActorContext => ActorRef,
        interval: Long = 60000,
        postedOnDaysBefore: Int = -8,
        postedOnHoursBefore: Int = -1) extends EchoedService {


    private val facebookAccess = facebookAccessCreator(context)

    private var scheduledMessage: Option[Cancellable] = None

    override def preStart() {
        next
    }

    def next {
        //interval less than one is used for testing - we will handle the message sending in a test
        if (interval > 0) self ! CrawlNext
    }

//    def findFacebookPostToCrawl = {
//        val postedOnStartDate = {
//            val cal = Calendar.getInstance()
//            cal.add(Calendar.DAY_OF_MONTH, postedOnDaysBefore)
//            cal.getTime
//        }
//
//        val postedOnEndDate = {
//            val cal = Calendar.getInstance()
//            cal.add(Calendar.HOUR, postedOnHoursBefore)
//            cal.getTime
//        }
//
//        val crawledOnEndDate = postedOnEndDate
//
//        Option(facebookPostDao.findPostToCrawl(postedOnStartDate, postedOnEndDate, crawledOnEndDate, null)).getOrElse {
//            Option(facebookPostDao.findOldPostToCrawl(postedOnStartDate, postedOnStartDate, null)).orNull
//        }
//    }

    protected def updateForCrawl(
            msg: GetPostDataResponse,
            facebookPost: FacebookPost,
            crawlStatus: String,
            retries: Int)(f: Unit => Unit) {
        try {
            log.debug("Updating post {} for crawl status {}, retries {}", facebookPost.id, crawlStatus, retries)
            scheduledMessage.foreach(_.cancel)
//            facebookPostDao.updatePostForCrawl(facebookPost.copy(crawledStatus = crawlStatus, crawledOn = new Date, retries = retries))
            f()
        } finally {
            next
        }
    }

    def handle = {
//        case CrawlNext =>
//            try {
//                Option(findFacebookPostToCrawl).cata({ facebookPostToCrawl =>
//                    log.debug("Found for crawling {}", facebookPostToCrawl)
//                    facebookAccess ! GetPostData(facebookPostToCrawl)
//                    facebookPostDao.updatePostForCrawl(facebookPostToCrawl.facebookPost.copy(
//                            crawledStatus = "started", crawledOn = new Date))
//                },
//                { log.debug("No posts found for crawling") })
//            } finally {
//                //always make sure we are looking for posts to crawl (less than one interval for testing)
//                if (interval > 0) scheduledMessage =
//                    Option(context.system.scheduler.scheduleOnce(interval milliseconds, context.self, CrawlNext))
//            }
//
//
//        case msg @ GetPostDataResponse(GetPostData(facebookPostToCrawl), Right(facebookPostData)) =>
//            updateForCrawl(msg, facebookPostToCrawl.facebookPost, "crawled", 0) { _ =>
//                facebookPostData.likes.foreach { facebookLikeDao.insertOrUpdate(_) }
//                facebookPostData.comments.foreach { fc => facebookCommentDao.insertOrUpdate(fc.copy(message = fc.message.take(1024))) }
//                log.debug("Received good response for FacebookPost {}", facebookPostData.facebookPost.id)
//            }

        case msg @ GetPostDataResponse(_, Left(GetPostDataOAuthError(facebookPost, _, _, message))) =>
            //don't bother retrying this crawl
            updateForCrawl(msg, facebookPost, message, retries = 1000) { _ =>
                log.debug("Received auth error crawling FacebookPost {}", facebookPost.id)
            }

        case msg @ GetPostDataResponse(_, Left(GetPostDataFalse(_, facebookPost))) =>
            //don't bother retrying this crawl
            updateForCrawl(msg, facebookPost, "false", retries = 1000) { _ =>
                log.debug("Received false response crawling FacebookPost {}", facebookPost.id)
            }

        case msg @ GetPostDataResponse(GetPostData(facebookPostToCrawl), Left(e: GetPostDataError )) => //facebookPost, t, c, m))) =>
            val message = "%s, type %s, code %s" format(e.m, e.errorType, e.code)
            updateForCrawl(msg, e.facebookPost, message, retries = facebookPostToCrawl.facebookPost.retries + 1) { _ =>
                log.debug("Received error response {} crawling FacebookPost {}", message, e.facebookPost.id)
            }

        case msg @ GetPostDataResponse(GetPostData(facebookPostToCrawl), Left(e)) =>
            updateForCrawl(msg, facebookPostToCrawl.facebookPost, e.message.take(254), facebookPostToCrawl.facebookPost.retries + 1) { _ =>
                log.debug("Received bad response crawling FacebookPost {}: {}", facebookPostToCrawl.id, e.message)
            }
    }

}

