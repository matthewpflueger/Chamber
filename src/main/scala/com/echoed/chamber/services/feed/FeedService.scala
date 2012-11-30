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
import com.echoed.chamber.domain.{Image, Topic, Community}
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

    var lookup = HashMap.empty[IndexKey, TreeMap[(Long, String), StoryPublic]]
    val storyMap = HashMap.empty[String, StoryPublic]
    var topStoryTree = new TreeMap[(Int, String), StoryPublic]()(TopStoryOrdering)
    var communityTree = new TreeMap[String, Community]()

    var imageMap = HashMap.empty[IndexKey, Image]

    private def addToLookup(indexKey: IndexKey, s: StoryPublic){
        val tree =  lookup.get(indexKey).getOrElse(new TreeMap[(Long, String), StoryPublic]()(StoryOrdering)) + ((s.story.updatedOn, s.story.id) -> s)
        lookup += (indexKey -> tree)

        Option(s.story.image).map {
            image => imageMap += (indexKey -> image)
        }
    }

    private def removeFromLookup(indexKey: IndexKey, s: StoryPublic){
        lookup.get(indexKey).map{
            t =>
                val tree = t - ((s.story.updatedOn, s.story.id))
                lookup += (indexKey -> tree)
        }
    }

    private def getStoriesFromLookup(indexKey: IndexKey, page: Int) = {
        val start = page * pageSize
        val stories = lookup.get(indexKey).map(_.values.map(s => if (indexKey.published) s.published else s)).flatten.toList
        val nextPage = getNextPage(start, page, stories)
        val image = getImageForKey(indexKey)
        PublicStoryFeed(image, stories.slice(start, start + pageSize), nextPage)
    }

    private def getImageForKey(indexKey: IndexKey) = {
        imageMap.get(indexKey).orNull
    }

    private def getStoryIdsFromLookup(indexKey: IndexKey) = {
        lookup.get(indexKey).map {
            _.keys.map(_._2)
        }.flatten.toArray
    }

    private def getNextPage(start: Int, page: Int, list: List[StoryPublic]) = {
        if (start + pageSize <= list.length) (page + 1).toString else null
    }

    def updateStory(storyFull: StoryPublic) {

        storyMap.get(storyFull.id).map { s =>

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
            Option(s.story.topicId).map{ tId => removeFromLookup(TopicKey(tId), s) }
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
                    Option(storyFull.story.topicId).map ( tId => addToLookup(TopicKey(tId), storyFull))
                    addToLookup(CommunityKey(storyFull.story.community), storyFull)
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

        case msg: GetCommunities =>
            sender ! GetCommunitiesResponse(msg, Right(new CommunityFeed(communityTree.values.toList)))

        case msg @ GetPublicStoryFeed(page) =>
            sender ! GetPublicStoryFeedResponse(msg, Right(getStoriesFromLookup(MainTreeKey(), msg.page)))

        case msg @ GetCategoryStoryFeed(categoryId, page) =>
            val feed = getStoriesFromLookup(CommunityKey(categoryId), msg.page)
            sender ! GetCategoryStoryFeedResponse(msg, Right(feed))

        case msg @ GetUserPrivateStoryFeed(echoedUserId, page) =>
            val feed = getStoriesFromLookup(EchoedUserPrivateKey(echoedUserId), msg.page)
            sender ! GetUserPrivateStoryFeedResponse(msg, Right(feed))

        case msg @ GetUserPublicStoryFeed(echoedUserId, page) =>
            val feed = getStoriesFromLookup(EchoedUserPublicKey(echoedUserId), msg.page)
            sender ! GetUserPublicStoryFeedResponse(msg, Right(feed))

        case msg @ RequestTopicStoryFeed(topicId, page) =>
            val feed = getStoriesFromLookup(TopicKey(topicId), msg.page)
            sender ! RequestTopicStoryFeedResponse(msg, Right(feed))

        case msg @ RequestPartnerStoryFeed(partnerId, page, origin) =>
            if (origin != "echoed") mp(WidgetOpened(partnerId))
            val feed = getStoriesFromLookup(PartnerKey(partnerId), msg.page)
            sender ! RequestPartnerStoryFeedResponse(msg,  Right(feed))


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
