package com.echoed.chamber.services.topic

import com.echoed.chamber.services._
import collection.mutable.HashMap
import collection.immutable.TreeMap
import feed.{RequestTopicStoryFeedResponse, RequestTopicStoryFeed}
import state.FindAllTopics
import state.FindAllTopicsResponse
import scala.Right
import com.echoed.chamber.domain.Topic
import com.echoed.chamber.domain.views.{ ContentFeed }
import com.echoed.chamber.services.partner.{TopicEvent, TopicUpdated, TopicCreated}
import com.echoed.chamber.domain.views.context.TopicContext

class TopicService(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem)  extends EchoedService {

    import context.dispatcher

    implicit object TopicOrdering extends Ordering[(String, String)] {
        def compare(a:(String, String), b:(String, String)) = {
            if((b._1 compare a._1) != 0)
                b._1 compare a._1
            else
                b._2 compare a._2
        }
    }

    class IndexKey(val id: String, val published: Boolean = true)
    case class PartnerKey(_id: String) extends IndexKey(_id)
    case class CommunityKey(_id: String) extends IndexKey(_id)
    case class MainTreeKey() extends IndexKey(null)

    var lookup = HashMap.empty[IndexKey, TreeMap[(String, String), Topic]]
    var topicMap = HashMap.empty[String, Topic]

    private def addToTopicLookup(indexKey: IndexKey, t: Topic){
        val tree = lookup.get(indexKey).getOrElse(new TreeMap[(String, String), Topic]()(TopicOrdering)) + ((t.title, t.id) -> t)
        lookup += (indexKey -> tree)
    }

    private def removeFromTopicLookup(indexKey: IndexKey, t: Topic){
        lookup.get(indexKey).map {
            t2 =>
                val tree = t2 - ((t.title, t.id))
                lookup += (indexKey -> tree)
        }
    }

    private def updateTopic(topic: Topic){
        topicMap.get(topic.id).map { t =>
            removeFromTopicLookup(PartnerKey(t.id), t)
            removeFromTopicLookup(CommunityKey(t.id), t)
        }

        topicMap += (topic.id -> topic)
        addToTopicLookup(PartnerKey(topic.id), topic)
        addToTopicLookup(CommunityKey(topic.id), topic)
    }

    private def getTopicsFromLookup(indexKey: IndexKey) = {
        lookup.get(indexKey).map(_.values).getOrElse(List[Topic]()).toList
    }

    override def preStart() {
        super.preStart()
//      ep.subscribe(context.self, classOf[TopicEvent])
        mp.tell(FindAllTopics(), self)
    }

    def handle = {

        case msg: TopicEvent => updateTopic(msg.topic)

        case FindAllTopicsResponse(_, Right(topics)) => topics.map(t => updateTopic(t))

        case msg @ ReadTopics() =>
            val topics = getTopicsFromLookup(MainTreeKey())
            sender ! ReadTopicsResponse(msg, Right(topics))

        case msg @ RequestTopic(topicId) =>
            topicMap.get(topicId).map(t => sender ! RequestTopicResponse(msg, Right(t)))

        case msg @ ReadCommunityTopics(communityId) =>
            val topics = getTopicsFromLookup(CommunityKey(communityId))
            sender ! ReadCommunityTopicsResponse(msg, Right(topics))

        case msg @ ReadTopicFeed(topicId, page) =>
            topicMap.get(topicId).map { topic =>
                mp(RequestTopicStoryFeed(topicId, page))
                    .mapTo[RequestTopicStoryFeedResponse]
                    .map(_.resultOrException)
                    .map(feed => sender ! ReadTopicFeedResponse(msg, Right(new ContentFeed[TopicContext](new TopicContext(topic), feed.content, feed.nextPage ))))
            }
    }
}
