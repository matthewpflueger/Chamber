package com.echoed.chamber.services.feed

import scala.collection.JavaConversions._
import akka.actor._
import com.echoed.chamber.services._
import collection.immutable.TreeMap
import echoeduser.{UpdateUserStory, StoryEvent, StoryViewed, EchoedUserClientCredentials}
import partner.{PartnerClientCredentials, UpdatePartnerStory}
import scala.collection.mutable.HashMap
import akka.pattern._
import com.echoed.chamber.services.state.QueryPartnerIdsResponse
import com.echoed.chamber.domain.Community
import com.echoed.chamber.domain.views.{Feed, CommunityFeed}
import state.FindAllStoriesResponse
import scala.Right
import com.echoed.chamber.services.state.QueryPartnerIds
import com.echoed.chamber.services.state.FindAllStories
import com.echoed.chamber.domain.public.StoryPublic
import com.echoed.chamber.domain.views.context.PublicContext


class FeedService(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem) extends EchoedService {

    import context.dispatcher

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

    class IndexKey(val id: String, val published: Boolean = true)
    case class EchoedUserKey(_id: String) extends IndexKey(_id)
    case class PartnerKey(_id: String) extends IndexKey(_id)
    case class CommunityKey(_id: String) extends IndexKey(_id)
    case class TopicKey(_id: String) extends IndexKey(_id)
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

    private def getStoriesFromLookup(indexKey: IndexKey, page: Int) = {
        val start = page * pageSize
        val stories = lookup
                .get(indexKey)
                .map(_.values.map(s => if (indexKey.published) s.published else s))
                .getOrElse(List[StoryPublic]())
                .toList
        val nextPage = getNextPage(start, page, stories)
        Feed(new PublicContext(), stories.slice(start, start + pageSize), nextPage)
    }

    private def getStoryIdsFromLookup(indexKey: IndexKey) = {
        lookup.get(indexKey).map(_.keys.map(_._2)).getOrElse(List[String]()).toArray
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
            removeFromLookup(EchoedUserKey(s.story.echoedUserId), s)
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
        addToLookup(EchoedUserKey(storyFull.story.echoedUserId), storyFull)
        addToLookup(PartnerKey(storyFull.story.partnerId), storyFull)
        if(!storyFull.isEchoedModerated) {
            addToLookup(MainTreeKey(), storyFull)
            Option(storyFull.story.topicId).map ( tId => addToLookup(TopicKey(tId), storyFull))
            addToLookup(CommunityKey(storyFull.story.community), storyFull)
        }

    }

    override def preStart() {
        super.preStart()
        ep.subscribe(context.self, classOf[Event])
        mp.tell(FindAllStories(), self)
    }

    def handle = {
        case msg: StoryEvent =>
            val s = new StoryPublic(msg.story.asStoryFull.get)
            updateStory(s)
            mp.tell(UpdateUserStory(EchoedUserClientCredentials(s.story.echoedUserId), s), self)
            mp.tell(UpdatePartnerStory(PartnerClientCredentials(s.story.partnerId), s), self)

        case FindAllStoriesResponse(_, Right(all)) => all.map(s => updateStory(new StoryPublic(s.asStoryFull.get)))

        case msg: GetCommunities =>
            sender ! GetCommunitiesResponse(msg, Right(new CommunityFeed(communityTree.values.toList)))

        case msg @ RequestPublicContent(page) =>
            sender ! RequestPublicContentResponse(msg, Right(getStoriesFromLookup(MainTreeKey(), msg.page)))

        case msg @ GetCategoryStoryFeed(categoryId, page) =>
            val feed = getStoriesFromLookup(CommunityKey(categoryId), msg.page)
            sender ! GetCategoryStoryFeedResponse(msg, Right(feed))

        case msg @ GetUserPrivateStoryFeed(echoedUserId, page) =>
            val feed = getStoriesFromLookup(EchoedUserKey(echoedUserId), msg.page)
            sender ! GetUserPrivateStoryFeedResponse(msg, Right(feed))

            case msg @ GetUserPublicStoryFeed(echoedUserId) =>
            val feed = getStoriesFromLookup(EchoedUserKey(echoedUserId), 0)
            sender ! GetUserPublicStoryFeedResponse(msg, Right(feed))

        case msg @ RequestTopicStoryFeed(topicId, page) =>
            val feed = getStoriesFromLookup(TopicKey(topicId), msg.page)
            sender ! RequestTopicStoryFeedResponse(msg, Right(feed))

        case msg @ RequestPartnerStoryFeed(partnerId) =>
            val feed = getStoriesFromLookup(PartnerKey(partnerId), 0)
            sender ! RequestPartnerStoryFeedResponse(msg,  Right(feed))


        case msg @ GetStory(storyId, origin) =>
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
