package com.echoed.chamber.services.feed

import scala.collection.JavaConversions._
import com.echoed.chamber.dao.views._
import akka.actor._
import com.echoed.chamber.dao.partner.PartnerDao
import com.echoed.chamber.dao.EchoedUserDao
import com.echoed.chamber.services._
import collection.immutable.TreeMap
import com.echoed.chamber.services.echoeduser.StoryEvent
import akka.pattern._
import scala.collection.mutable.HashMap
import scala.Left
import com.echoed.chamber.services.state.FindAllStoriesResponse
import com.echoed.chamber.services.event.WidgetStoryOpened
import com.echoed.chamber.services.event.WidgetOpened
import com.echoed.chamber.domain.views.PublicStoryFeed
import com.echoed.chamber.domain.views.EchoedUserStoryFeed
import com.echoed.chamber.domain.public.StoryPublic
import com.echoed.chamber.domain.views.PartnerStoryFeed
import scala.Right
import com.echoed.chamber.services.state.FindAllStories
import com.echoed.chamber.domain.public.EchoedUserPublic
import com.echoed.chamber.domain.public.PartnerPublic


class FeedService(
        feedDao: FeedDao,
        partnerDao: PartnerDao,
        echoedUserDao: EchoedUserDao,
        mp: MessageProcessor,
        ep: EventProcessorActorSystem) extends EchoedService {

    val pageSize = 30

    implicit object StoryOrdering extends Ordering[(Long, String)] {
        def compare(a:(Long, String), b:(Long, String)) = {
            if((b._1 compare a._1) != 0)
                b._1 compare a._1
            else
                b._2 compare a._2
        }
    }

    implicit object TopStoryOrdering extends Ordering[(Int, String)] {
        def compare(a:(Int, String), b:(Int, String)) = {
            if((b._1 compare a._1) != 0)
                b._1 compare a._1
            else
                b._2 compare a._2
        }
    }

    val storyMap = HashMap.empty[String, StoryPublic]
    var storyTree = new TreeMap[(Long, String), StoryPublic]()(StoryOrdering)
    var topStoryTree = new TreeMap[(Int, String), StoryPublic]()(TopStoryOrdering)

    def updateStory(storyFull: StoryPublic) {
        storyMap.get(storyFull.id).map { s =>
            topStoryTree -= ((s.comments.length + s.chapterImages.length, s.story.id))
            storyTree -= ((s.story.updatedOn, s.story.id))
        }
        storyMap += (storyFull.id -> storyFull)
        storyTree += ((storyFull.story.updatedOn, storyFull.story.id) -> storyFull)

        if (!storyFull.isEchoedModerated) {
            topStoryTree += ((storyFull.comments.length + storyFull.chapterImages.length, storyFull.story.id) -> storyFull)
        }
    }


    override def preStart() {
        super.preStart()
        ep.subscribe(context.self, classOf[Event])
        mp.tell(FindAllStories(), self)
    }

    def handle = {
        case msg: StoryEvent =>
            log.debug("Received StoryEvent {}", msg)
            updateStory(new StoryPublic(msg.story.asStoryFull.get))

        case FindAllStoriesResponse(_, Right(all)) => all.map(s => updateStory(new StoryPublic(s.asStoryFull.get)))

        case msg @ GetPublicStoryFeed(page: Int) =>
            val channel = context.sender
            val start = msg.page * pageSize
            try {
                log.debug("Attempting to retrieve Public Story Feed")
                val stories = topStoryTree.values.toList
                val nextPage = {
                    if(start + pageSize <= stories.length)
                        (msg.page + 1).toString
                    else
                        null
                }
                val feed = PublicStoryFeed(stories.slice(start, start + pageSize), nextPage)
                channel ! GetPublicStoryFeedResponse(msg, Right(feed))
            } catch {
                case e =>
                    channel ! GetPublicStoryFeedResponse(msg, Left(new FeedException("Cannot get public story feed", e)))
                    log.error("Unexpected error processing {}, {}", msg, e)
            }

        case msg @ GetCategoryStoryFeed(categoryId, page) =>
            val channel = context.sender
            val start = msg.page * pageSize

            val catId = categoryId.toLowerCase

            try {
                log.debug("Attempting to retrieve Stories for Category: {}", catId)
                val stories = storyTree.values.filter { s =>
                    !s.isEchoedModerated && Option(s.story.tag).map(_.toLowerCase == catId).getOrElse(false)
                }.toList
                log.debug("Stores Tagged: {}", stories)
                val nextPage = {
                    if(start + pageSize <= stories.length)
                        (msg.page + 1).toString
                    else
                        null
                }
                val feed = new PublicStoryFeed(stories.slice(start, start + pageSize), nextPage)

                channel ! GetCategoryStoryFeedResponse(msg, Right(feed))
            } catch {
                case e =>
                    channel ! GetCategoryStoryFeedResponse(msg, Left(new FeedException("Cannot get public category feed", e)))
                    log.error("Unexpected Error processing {}, {}", msg , e)
            }


        case msg @ GetUserPublicStoryFeed(echoedUserId, page) =>
            val channel = context.sender
            val start = msg.page * pageSize
            try {
                log.debug("Attempting to retrieve story feed for user: {}", echoedUserId)
                val stories = storyTree.values.filter(_.echoedUser.id.equals(echoedUserId)).toList
                val nextPage = {
                    if(start + pageSize <= stories.length)
                        (msg.page + 1).toString
                    else
                        null
                }
                val feed = new EchoedUserStoryFeed(
                        Option(echoedUserDao.findById(echoedUserId)).map(new EchoedUserPublic(_)).orNull,
                        stories.slice(start, start + pageSize),
                        nextPage)
                channel ! GetUserPublicStoryFeedResponse(msg, Right(feed))
            } catch {
                case e =>
                    channel ! GetUserPublicStoryFeedResponse(msg, Left(new FeedException("Cannot get user public story feed", e)))
                    log.error(e, "Unexpected error processesiong {}", msg)
            }


        case msg @ GetPartnerStoryFeed(partnerId, page, origin) =>
            if (origin != "echoed") mp(WidgetOpened(partnerId))

            val channel = context.sender
            try {
                val start = msg.page * pageSize
                val partner = partnerDao.findByIdOrHandle(msg.partnerId)
                log.debug("Looking up stories for Partner Id {}", partner.id)
                val stories = storyTree.values.filter(s => s.story.partnerId.equals(partner.id) && !s.isModerated).toList
                val nextPage = {
                    if(start + pageSize <= stories.length)
                        (msg.page + 1).toString
                    else
                        null
                }
                val partnerFeed = new PartnerStoryFeed(new PartnerPublic(partner), stories.slice(start, start + pageSize), nextPage)
                channel ! GetPartnerStoryFeedResponse(msg, Right(partnerFeed))
            } catch {
                case e =>
                    channel ! GetPartnerStoryFeedResponse(msg, Left(new FeedException("Cannot get partner story feed", e)))
                    log.error("Unexpected error processing {} , {}", msg, e)
            }

        case msg @ GetStory(storyId, origin) =>
            if (origin != "echoed") mp(WidgetStoryOpened(storyId))

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
