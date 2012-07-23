package com.echoed.chamber.services.feed

import scala.collection.JavaConversions._
import com.echoed.chamber.dao.views._
import akka.actor._
import akka.actor.{ActorSystem, Props, ActorRef, Actor}
import com.echoed.chamber.dao.partner.PartnerDao
import com.echoed.chamber.dao.EchoedUserDao
import com.echoed.chamber.domain.public._
import scala.Right
import com.echoed.chamber.domain.views._
import scala.Left
import com.echoed.chamber.domain.views.PublicFeed
import com.echoed.chamber.services.{EventProcessorActorSystem, EchoedActor}
import collection.immutable.{HashMap, TreeSet, SortedSet, TreeMap}


class FeedServiceActor(
        feedDao: FeedDao,
        partnerDao: PartnerDao,
        echoedUserDao: EchoedUserDao,
        eventProcessor: EventProcessorActorSystem) extends EchoedActor {

    val pageSize = 30

    implicit object StoryOrdering extends Ordering[(Long, String)] {
        def compare(a:(Long, String), b:(Long, String)) = {
            Option(b._1 compare a._1).getOrElse(b._2 compare a._2)
        }
    }

    var storyMap = new HashMap[String, StoryFull]
    var storyTree = new TreeMap[(Long, String), StoryFull]()(StoryOrdering)
    val stories = asScalaBuffer(feedDao.getAllStories).map(updateStory(_))

    def updateStory(storyFull: StoryFull) {
        storyMap.get(storyFull.id).map(s => storyTree -= ((storyFull.story.updatedOn, storyFull.story.id)))
        storyMap += (storyFull.id -> storyFull)
        storyTree += ((storyFull.story.updatedOn, storyFull.story.id) -> storyFull)
    }

    eventProcessor.subscribe(self, classOf[StoryUpdated])

    def handle = {

        case msg @ StoryUpdated(storyId: String) =>
            log.debug("Story Updated: {}", storyId)
            Option(feedDao.findStoryById(storyId)).map(updateStory(_))
            //CURRENTLY HITS DATABASE ONCE STORY HAS BEEN UPDATED TO GRAB FULL STORY

        case msg @ GetPublicFeed(page: Int) =>
            val channel = context.sender
            val start = msg.page * pageSize

            try {
                log.debug("Attempting to retrieve Public Feed ")
                val echoes = asScalaBuffer(feedDao.getPublicFeed(start, pageSize)).toList
                val stories = asScalaBuffer(feedDao.getStories(start, pageSize))
                val feed = new PublicFeed(echoes, stories)
                channel ! GetPublicFeedResponse(msg, Right(feed))
            } catch {
                case e=>
                    channel ! GetPublicFeedResponse(msg, Left(new FeedException("Cannot get public feed", e)))
                    log.error("Unexpected error processing {} , {}", msg, e)
            }

        case msg @ GetPublicStoryFeed(page: Int) =>
            val channel = context.sender
            val start = msg.page * pageSize
            try {
                log.debug("Attempting to retrieve Public Story Feed")
                val feed = PublicStoryFeed(storyTree.values.toList.slice(start, start + pageSize))
                channel ! GetPublicStoryFeedResponse(msg, Right(feed))
            } catch {
                case e =>
                    channel ! GetPublicStoryFeedResponse(msg, Left(new FeedException("Cannot get public story feed", e)))
                    log.error("Unexpected error processing {}, {}", msg, e)
            }

        case msg @ GetPublicCategoryFeed(categoryId: String, page: Int) =>
            val channel = context.sender
            val start = msg.page * pageSize

            try{
                log.debug("Attempting to retrive Category Feed")
                val echoes = asScalaBuffer(feedDao.getCategoryFeed(categoryId, start, pageSize))
                val feed = new PublicFeed(echoes)
                channel ! GetPublicCategoryFeedResponse(msg, Right(feed))
            } catch {
                case e =>
                    channel ! GetPublicCategoryFeedResponse(msg, Left(new FeedException("Cannot get public category feed", e)))
                    log.error("Unpexected Error processing {}, {}", msg, e)
            }



        case msg @ GetUserPublicFeed(echoedUserId: String, page:Int) =>
            val channel = context.sender
            val start = msg.page * pageSize
            try {
                log.debug("Attempting to retrieve feed for user: ")
                val echoedUser = echoedUserDao.findById(echoedUserId)
                val echoes = asScalaBuffer(feedDao.getEchoedUserFeed(echoedUser.id, start, pageSize)).toList
                val stories = asScalaBuffer(feedDao.findStoryByEchoedUserId(echoedUser.id, start, pageSize)).toList
                val feed = new EchoedUserFeed(new EchoedUserPublic(echoedUser), echoes, stories)
                channel ! GetUserPublicFeedResponse(msg, Right(feed))
            } catch {
                case e =>
                    channel ! GetUserPublicFeedResponse(msg, Left(new FeedException("Cannot get user public feed", e)))
                    log.error("Unexpected error processesing {}, {}", msg, e)
            }

        case msg @ GetUserPublicStoryFeed(echoedUserId: String, page: Int) =>
            val channel = context.sender
            val start = msg.page * pageSize
            try {
                log.debug("Attempting to retrieve story feed for user: {}", echoedUserId)
                val echoedUser = echoedUserDao.findById(echoedUserId)
                val stories = storyTree.values.filter(_.echoedUser.id.equals(echoedUser.id)).toList.slice(start, start + pageSize)
                val feed = new EchoedUserStoryFeed(new EchoedUserPublic(echoedUser), stories)
                channel ! GetUserPublicStoryFeedResponse(msg, Right(feed))
            } catch {
                case e =>
                    channel ! GetUserPublicStoryFeedResponse(msg, Left(new FeedException("Cannot get user public story feed", e)))
                    log.error("Unexpected error processesiong {}, {}", msg, e)
            }

        case msg @ GetPartnerFeed(partnerId: String, page: Int) =>
            val channel = context.sender
            try{
                val start = page * pageSize
                val echoes = asScalaBuffer(feedDao.getPartnerFeed(partnerId, start, pageSize)).toList
                val partner = partnerDao.findByIdOrHandle(msg.partnerId)
                val stories = asScalaBuffer(feedDao.findStoryByPartnerId(partner.id, start, pageSize)).toList
                val partnerFeed = new PartnerFeed(new PartnerPublic(partner), echoes, stories)
                channel ! GetPartnerFeedResponse(msg,Right(partnerFeed))
            } catch {
                case e =>
                    channel ! GetPartnerFeedResponse(msg, Left(new FeedException("Cannot get partner feed", e)))
                    log.error("Unexpected error processesing {}, {}", msg, e)
            }

        case msg @ GetPartnerStoryFeed(partnerId: String, page: Int) =>
            val channel = context.sender
            try {
                val start = msg.page * pageSize
                val partner = partnerDao.findByIdOrHandle(msg.partnerId)
                log.debug("Looking up stories for Partner Id {}", partner.id)
                val stories = storyTree.values.filter(_.story.partnerId.equals(partner.id)).toList.slice(start, start + pageSize)
                val partnerFeed = new PartnerStoryFeed(new PartnerPublic(partner), stories)
                channel ! GetPartnerStoryFeedResponse(msg, Right(partnerFeed))
            } catch {
                case e =>
                    channel ! GetPartnerStoryFeedResponse(msg, Left(new FeedException("Cannot get partner story feed", e)))
                    log.error("Unexpected error processing {} , {}", msg, e)
            }

        case msg @ GetStory(storyId) =>
            val channel = context.sender
            try {
                channel ! GetStoryResponse(msg, Right(storyMap.get(storyId)))
            } catch {
                case e =>
                    channel ! GetStoryResponse(msg, Left(new FeedException("Cannot get story %s" format storyId, e)))
                    log.error("Unexpected error processing {}: {}", msg, e)
            }

        case msg: GetStoryIds =>
            val channel = context.sender
            channel ! GetStoryIdsResponse(msg, Right(storyTree.keys.map(_._2).toArray))

        case msg: GetPartnerIds =>
            val channel = context.sender
            channel ! GetPartnerIdsResponse(msg, Right(feedDao.getPartnerIds))

    }

}
