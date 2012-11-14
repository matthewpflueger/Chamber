package com.echoed.chamber.services.feed

import scala.collection.JavaConversions._
import com.echoed.chamber.dao.views._
import akka.actor._
import com.echoed.chamber.dao.partner.PartnerDao
import com.echoed.chamber.dao.EchoedUserDao
import com.echoed.chamber.services._
import collection.immutable.TreeMap
import com.echoed.chamber.services.echoeduser.{EchoedUserClientCredentials, StoryViewed, StoryEvent}
import scala.collection.mutable.HashMap
import scala.Left
import com.echoed.chamber.services.state.FindAllStoriesResponse
import com.echoed.chamber.services.event.WidgetStoryOpened
import com.echoed.chamber.services.event.WidgetOpened
import com.echoed.chamber.domain.views.{CommunityFeed, PublicStoryFeed, EchoedUserStoryFeed, PartnerStoryFeed}
import com.echoed.chamber.domain.public.StoryPublic
import scala.Right
import com.echoed.chamber.services.state.FindAllStories
import com.echoed.chamber.domain.public.PartnerPublic
import com.echoed.chamber.domain.Community


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

    case class IndexKey(id: String, published: Boolean = true)
    case class EchoedUserPublicKey(_id: String) extends IndexKey(_id)
    case class EchoedUserPrivateKey(_id: String) extends IndexKey(_id, false)
    case class PartnerKey(_id: String) extends IndexKey(_id)
    case class CommunityKey(_id: String) extends IndexKey(_id)
    case class MainTreeKey() extends IndexKey(null)

    var lookup = HashMap.empty[IndexKey, TreeMap[(Long, String), StoryPublic]]

    val storyMap = HashMap.empty[String, StoryPublic]

    var topStoryTree = new TreeMap[(Int, String), StoryPublic]()(TopStoryOrdering)

    var communityTree = new TreeMap[String, Community]()

    private def addToLookup(indexKey: IndexKey, s: StoryPublic){
        val tree =  lookup.get(indexKey).getOrElse(new TreeMap[(Long, String), StoryPublic]()(StoryOrdering)) + ((s.story.updatedOn, s.story.id) -> s)
        lookup += (indexKey -> tree)
    }

    private def removeFromLookup(indexKey: IndexKey, s: StoryPublic){
        lookup.get(indexKey).map{
            t =>
                val tree = t - ((s.story.updatedOn, s.story.id))
                lookup += (indexKey -> tree)
        }
    }

    private def getStoriesFromLookup(indexKey: IndexKey) = {
        lookup.get(indexKey).map(_.values.map{
            s =>
                if(indexKey.published) s.published
                else s
        }).flatten.toList
    }

    private def getStoryIdsFromLookup(indexKey: IndexKey) = {
        lookup.get(indexKey).map {
            _.keys.map(_._2)
        }.flatten.toArray
    }

    private def getNextPage(start: Int, page: Int, list: List[StoryPublic]) = {
        if(start + pageSize <= list .length) (page + 1).toString
        else null
    }

    def updateStory(storyFull: StoryPublic) {

        storyMap.get(storyFull.id).map { s =>
            topStoryTree -= ((s.comments.length + s.chapterImages.length, s.story.id))

            if(s.isPublished && !s.isEchoedModerated && s.story.community != null && s.story.community != ""){
                communityTree.get(s.story.community).map {
                    c => {
                        communityTree += (c.id -> c.copy(counter = c.counter - 1))
                    }
                }
            }

            removeFromLookup(MainTreeKey(), s)
            removeFromLookup(EchoedUserPrivateKey(s.story.echoedUserId), s)
            removeFromLookup(EchoedUserPublicKey(s.story.echoedUserId), s)
            removeFromLookup(PartnerKey(s.story.partnerId), s)
            removeFromLookup(CommunityKey(s.story.community), s)
        }

        if(storyFull.isPublished && !storyFull.isEchoedModerated && storyFull.story.community != null && storyFull.story.community != ""){
            val community = communityTree.get(storyFull.story.community)
                .getOrElse(new Community(storyFull.story.community, 0, true))
            communityTree += (community.id -> community.copy(counter = community.counter + 1))
        }

        storyMap += (storyFull.id -> storyFull)

        if(!storyFull.isSelfModerated){
            addToLookup(EchoedUserPrivateKey(storyFull.story.echoedUserId), storyFull)

            if(storyFull.isPublished){

                addToLookup(EchoedUserPublicKey(storyFull.story.echoedUserId), storyFull)

                if(!storyFull.isEchoedModerated) {
                    addToLookup(MainTreeKey(), storyFull)
                    addToLookup(CommunityKey(storyFull.story.community), storyFull)
                    topStoryTree += ((storyFull.comments.length + storyFull.chapterImages.length, storyFull.story.id) -> storyFull)
                }

                if (!storyFull.isModerated) {
                    addToLookup(PartnerKey(storyFull.story.partnerId), storyFull)
                }
            }
        }
    }

    override def preStart() {
        super.preStart()
        ep.subscribe(context.self, classOf[Event])
        mp.tell(FindAllStories(), self)
    }

    def handle = {
        case msg: StoryEvent =>
            updateStory(new StoryPublic(msg.story.asStoryFull.get))

        case FindAllStoriesResponse(_, Right(all)) => all.map(s => updateStory(new StoryPublic(s.asStoryFull.get)))

        case msg @ GetCommunities() =>
            val channel = context.sender
            try {
                val communities = communityTree.values.toList
                channel ! GetCommunitiesResponse(msg, Right(new CommunityFeed(communities)))
            } catch {
                case e =>
                    channel ! GetCommunitiesResponse(msg, Left(new FeedException("Cannot Get Communities", e)))
                    log.error("Unexpected error processing {}, {}", msg, e)
            }

        case msg @ GetPublicStoryFeed(page: Int) =>
            val channel = context.sender
            val start = msg.page * pageSize
            try {
                log.debug("Attempting to retrieve Public Story Feed")
                val stories = getStoriesFromLookup(MainTreeKey())
                val nextPage = getNextPage(start, page, stories)

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

                val stories = getStoriesFromLookup(CommunityKey(categoryId))
                val nextPage = getNextPage(start, page, stories)
                val feed = new PublicStoryFeed(stories.slice(start, start + pageSize), nextPage)

                channel ! GetCategoryStoryFeedResponse(msg, Right(feed))
            } catch {
                case e =>
                    channel ! GetCategoryStoryFeedResponse(msg, Left(new FeedException("Cannot get public category feed", e)))
                    log.error("Unexpected Error processing {}, {}", msg , e)
            }

        case msg @ GetUserPrivateStoryFeed(echoedUserId, page) =>
            val start = msg.page * pageSize
            val stories = getStoriesFromLookup(EchoedUserPrivateKey(echoedUserId))
            val nextPage = getNextPage(start, page, stories)
            val feed = new PublicStoryFeed(stories.slice(start, start + pageSize), nextPage)
            sender ! GetUserPrivateStoryFeedResponse(msg, Right(feed))

        case msg @ GetUserPublicStoryFeed(echoedUserId, page) =>
            val start = msg.page * pageSize

            val stories = getStoriesFromLookup(EchoedUserPublicKey(echoedUserId))
            val nextPage = getNextPage(start, page, stories)

            val feed = new PublicStoryFeed(stories.slice(start, start + pageSize), nextPage)
            sender ! GetUserPublicStoryFeedResponse(msg, Right(feed))


        case msg @ GetPartnerStoryFeed(partnerId, page, origin) =>
            if (origin != "echoed") mp(WidgetOpened(partnerId))

            val channel = context.sender
            try {
                val start = msg.page * pageSize
                val partner = partnerDao.findByIdOrHandle(msg.partnerId)
                log.debug("Looking up stories for Partner Id {}", partner.id)

                val stories = getStoriesFromLookup(PartnerKey(partner.id))
                val nextPage = getNextPage(start, page, stories)


            val partnerFeed = new PartnerStoryFeed(new PartnerPublic(partner), stories.slice(start, start + pageSize), nextPage)
                channel ! GetPartnerStoryFeedResponse(msg, Right(partnerFeed))
            } catch {
                case e =>
                    channel ! GetPartnerStoryFeedResponse(msg, Left(new FeedException("Cannot get partner story feed", e)))
                    log.error("Unexpected error processing {} , {}", msg, e)
            }

        case msg @ GetStory(storyId, origin) =>
            if (origin != "echoed") mp(WidgetStoryOpened(storyId))
            sender ! GetStoryResponse(msg, Right(storyMap.get(storyId).map { sp =>
                mp(StoryViewed(EchoedUserClientCredentials(sp.echoedUser.id), storyId)); sp.published
            }))

        case msg: GetStoryIds =>
            val channel = context.sender
            channel ! GetStoryIdsResponse(msg, Right(getStoryIdsFromLookup(MainTreeKey())))

        case msg: GetPartnerIds =>
            val channel = context.sender
            channel ! GetPartnerIdsResponse(msg, Right(feedDao.getPartnerIds))
    }

}
