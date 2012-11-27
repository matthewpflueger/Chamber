package com.echoed.chamber.services.feed

import scala.collection.JavaConversions._
import akka.actor._
import com.echoed.chamber.services._
import collection.immutable.TreeMap
import com.echoed.chamber.services.echoeduser.StoryEvent
import scala.collection.mutable.HashMap
import com.echoed.chamber.services.state._
import akka.pattern._
import com.echoed.chamber.services.event.WidgetStoryOpened
import com.echoed.chamber.services.state.QueryPartnerIdsResponse
import com.echoed.chamber.services.event.WidgetOpened
import com.echoed.chamber.domain.views._
import scala.Left
import com.echoed.chamber.domain.{Topic, Community}
import com.echoed.chamber.domain.views.CommunityFeed
import com.echoed.chamber.domain.views.PublicStoryFeed
import com.echoed.chamber.domain.public.PartnerPublic
import com.echoed.chamber.domain.public.{TopicPublic, PartnerPublic, StoryPublic}
import state.FindAllStories
import state.FindAllStoriesResponse
import com.echoed.chamber.services.echoeduser.StoryViewed
import com.echoed.chamber.domain.views.PartnerStoryFeed
import scala.Right
import com.echoed.chamber.services.state.QueryPartnerIds
import com.echoed.chamber.services.state.FindAllStories
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials


class FeedService(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem) extends EchoedService {

    val pageSize = 30

    implicit object TopicOrdering extends Ordering[(String, String)] {
        def compare(a:(String, String), b:(String, String)) = {
            if((b._1 compare a._1) != 0)
                b._1 compare a._1
            else
                b._2 compare a._2
        }
    }

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
    case class EchoedUserPrivateKey(_id: String) extends IndexKey(_id) //, false)
    case class PartnerKey(_id: String) extends IndexKey(_id)
    case class CommunityKey(_id: String) extends IndexKey(_id)
    case class TopicKey(_id: String) extends IndexKey(_id)
    case class MainTreeKey() extends IndexKey(null)

    var topicLookup = HashMap.empty[IndexKey, TreeMap[(String, String), Topic]]

    private def addToTopicLookup(indexKey: IndexKey, t: Topic){
        val tree = topicLookup.get(indexKey).getOrElse(new TreeMap[(String, String), Topic]()(TopicOrdering)) + ((t.title, t.id) -> t)
        topicLookup += (indexKey -> tree)
    }

    private def removeFromTopicLookup(indexKey: IndexKey, t: Topic){
        topicLookup.get(indexKey).map {
            t2 =>
                val tree = t2 - ((t.title, t.id))
                topicLookup += (indexKey -> tree)
        }
    }

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
//            topStoryTree -= ((s.comments.length + s.chapterImages.length, s.story.id))

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
            removeFromLookup(TopicKey("01"), s)
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
                    addToLookup(TopicKey("01"), storyFull)
                    addToLookup(CommunityKey(storyFull.story.community), storyFull)
//                    topStoryTree += ((storyFull.comments.length + storyFull.chapterImages.length, storyFull.story.id) -> storyFull)
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

        case msg @ GetTopicStoryFeed(topicId, page) =>
            val channel = context.sender
            val start = msg.page * pageSize
            try {
                val stories = getStoriesFromLookup(TopicKey(topicId))
                val nextPage = getNextPage(start, page, stories)
                val feed = new TopicStoryFeed(null, stories.slice(start, start + pageSize), nextPage)
                channel ! GetTopicStoryFeedResponse(msg, Right(feed))
            } catch {
                case e =>
                    channel ! GetTopicStoryFeedResponse(msg, Left(new FeedException("Cannot get topic feed", e)))
                    log.error("Unpexpected Error processing {}, {}", msg, e)
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

            mp(QueryPartnerByIdOrHandle(msg.partnerId))
                    .mapTo[QueryPartnerByIdOrHandleResponse]
                    .map(_.resultOrException)
                    .map { p =>
                        val start = msg.page * pageSize
                        val stories = getStoriesFromLookup(PartnerKey(p.id))
                        val nextPage = getNextPage(start, page, stories)
                        GetPartnerStoryFeedResponse(msg,  Right(new PartnerStoryFeed(
                                new PartnerPublic(p),
                                stories.slice(start, start + pageSize),
                                nextPage)))
                    }.pipeTo(sender)


        case msg @ GetStory(storyId, origin) =>
            if (origin != "echoed") mp(WidgetStoryOpened(storyId))
            sender ! GetStoryResponse(msg, Right(storyMap.get(storyId).map { sp =>
                mp(StoryViewed(EchoedUserClientCredentials(sp.echoedUser.id), storyId)); sp.published
            }))

        case msg: GetStoryIds =>
            sender ! GetStoryIdsResponse(msg, Right(getStoryIdsFromLookup(MainTreeKey())))

        case msg: GetPartnerIds =>
            mp(QueryPartnerIds())
                    .mapTo[QueryPartnerIdsResponse]
                    .map(_.resultOrException)
                    .map(r => GetPartnerIdsResponse(msg, Right(r)))
                    .pipeTo(sender)
    }

}
