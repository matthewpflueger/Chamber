package com.echoed.chamber.services.feed

import scala.collection.JavaConversions._
import com.echoed.chamber.dao.views._
import akka.actor._
import com.echoed.chamber.dao.partner.PartnerDao
import com.echoed.chamber.dao.EchoedUserDao
import com.echoed.chamber.domain.public.EchoedUserPublic
import com.echoed.chamber.domain.public.PartnerPublic
import scala.Right
import com.echoed.chamber.domain.views.PartnerFeed
import com.echoed.chamber.domain.views.EchoedUserFeed
import scala.Left
import com.echoed.chamber.domain.views.PublicFeed


class FeedServiceActor(
        feedDao: FeedDao,
        partnerDao: PartnerDao,
        echoedUserDao: EchoedUserDao) extends Actor with ActorLogging {

    def receive = {
        case msg @ GetPublicFeed(page: Int) =>
            val channel = context.sender
            val limit = 30
            val start = msg.page * limit

            try {
                log.debug("Attempting to retrieve Public Feed ")
                val echoes = asScalaBuffer(feedDao.getPublicFeed(start,limit)).toList
                val stories = asScalaBuffer(feedDao.getStories(start,limit))
                val feed = new PublicFeed(echoes, stories)
                channel ! GetPublicFeedResponse(msg, Right(feed))
            } catch {
                case e=>
                    channel ! GetPublicFeedResponse(msg, Left(new FeedException("Cannot get public feed", e)))
                    log.error("Unexpected error processing {} , {}", msg, e)
            }

        case msg @ GetPublicCategoryFeed(categoryId: String, page: Int) =>
            val channel = context.sender
            val limit = 30
            val start = msg.page * limit

            try{
                log.debug("Attempting to retrive Category Feed")
                val echoes = asScalaBuffer(feedDao.getCategoryFeed(categoryId, start, limit))
                val stories = asScalaBuffer(feedDao.getStories(start,limit))
                val feed = new PublicFeed(echoes)
                channel ! GetPublicCategoryFeedResponse(msg, Right(feed))
            } catch {
                case e =>
                    channel ! GetPublicCategoryFeedResponse(msg, Left(new FeedException("Cannot get public category feed", e)))
                    log.error("Unpexected Error processing {}, {}", msg, e)
            }


        case msg @ GetUserPublicFeed(echoedUserId: String, page:Int) =>
            val channel = context.sender
            val limit = 30
            val start = msg.page * limit
            try {
                log.debug("Attempting to retrieve feed for user: ")
                val echoedUser = echoedUserDao.findById(echoedUserId)
                val echoes = asScalaBuffer(feedDao.getEchoedUserFeed(echoedUser.id, start, limit)).toList
                val stories = asScalaBuffer(feedDao.findStoryByEchoedUserId(echoedUser.id)).toList
                val feed = new EchoedUserFeed(new EchoedUserPublic(echoedUser), echoes, stories)
                channel ! GetUserPublicFeedResponse(msg, Right(feed))
            } catch {
                case e =>
                    channel ! GetUserPublicFeedResponse(msg, Left(new FeedException("Cannot get user public feed", e)))
                    log.error("Unexpected error processesing {}, {}", msg, e)
            }

        case msg @ GetPartnerFeed(partnerId: String, page: Int) =>
            val channel = context.sender
            try{
                val partnerId = msg.partnerId
                val limit = 30
                val start = msg.page * limit
                val echoes = asScalaBuffer(feedDao.getPartnerFeed(partnerId, start, limit)).toList
                val partner = partnerDao.findByIdOrHandle(partnerId)
                val stories = asScalaBuffer(feedDao.findStoryByPartnerId(partner.id)).toList
                val partnerFeed = new PartnerFeed(new PartnerPublic(partner), echoes, stories)
                channel ! GetPartnerFeedResponse(msg,Right(partnerFeed))
            } catch {
                case e =>
                    channel ! GetPartnerFeedResponse(msg, Left(new FeedException("Cannot get partner feed", e)))
                    log.error("Unexpected error processesing {}, {}", msg, e)
            }

        case msg @ GetStory(storyId) =>
            val channel = context.sender

            try {
                channel ! GetStoryResponse(msg, Right(Option(feedDao.findStoryById(storyId))))
            } catch {
                case e =>
                    channel ! GetStoryResponse(msg, Left(new FeedException("Cannot get story %s" format storyId, e)))
                    log.error("Unexpected error processing {}: {}", msg, e)
            }

        case msg: GetStoryIds =>
            val channel = context.sender
            channel ! GetStoryIdsResponse(msg, Right(feedDao.getStoryIds))

        case msg: GetPartnerIds =>
            val channel = context.sender
            channel ! GetPartnerIdsResponse(msg, Right(feedDao.getPartnerIds))

    }

}
