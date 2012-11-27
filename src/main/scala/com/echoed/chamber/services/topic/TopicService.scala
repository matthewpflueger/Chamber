package com.echoed.chamber.services.topic

import com.echoed.chamber.services._
import collection.mutable.HashMap
import collection.immutable.TreeMap
import feed.{RequestTopicStoryFeedResponse, RequestTopicStoryFeed}
import state.FindAllTopics
import state.FindAllTopicsResponse
import scala.Right
import com.echoed.chamber.domain.Topic
import com.echoed.chamber.domain.views.TopicStoryFeed

class TopicService(
    mp: MessageProcessor,
    ep: EventProcessorActorSystem)  extends EchoedService {

    implicit object TopicOrdering extends Ordering[(String, String)] {
        def compare(a:(String, String), b:(String, String)) = {
            if((b._1 compare a._1) != 0)
                b._1 compare a._1
            else
                b._2 compare a._2
        }
    }

    case class IndexKey(id: String, published: Boolean = true)
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

    private def updateTopic(t: Topic){
        val key = t.refType match {
            case "Echoed" => MainTreeKey()
            case "Partner" => PartnerKey(t.refId)
            case "Community" => CommunityKey(t.refId)
        }

        topicMap.get(t.id).map {
            t2 => removeFromTopicLookup(key, t2)
        }

        topicMap += (t.id -> t)
        addToTopicLookup(key, t)
    }

    private def getTopicsFromLookup(indexKey: IndexKey) = {
        lookup.get(indexKey).map(_.values).flatten.toList
    }

    override def preStart() {
        super.preStart()
        ep.subscribe(context.self, classOf[Event])
        mp.tell(FindAllTopics(), self)
    }

    def handle = {

        case FindAllTopicsResponse(_, Right(topics)) => topics.map( t => updateTopic(t))

        case msg @ GetTopics() =>
            val channel = context.sender
            val topics = getTopicsFromLookup(MainTreeKey())
            channel ! GetTopicsResponse(msg, Right(topics))

        case msg @ RequestPartnerTopics(partnerId) =>
            val channel = context.sender
            val topics = getTopicsFromLookup(PartnerKey(partnerId))
            channel ! RequestPartnerTopicsResponse(msg, Right(topics))

        case msg @ ReadCommunityTopics(communityId) =>
            val channel = context.sender
            val topics = getTopicsFromLookup(CommunityKey(communityId))
            channel ! ReadCommunityTopicsResponse(msg, Right(topics))

        case msg @ ReadTopicFeed(topicId, page) =>
            val channel = context.sender
            topicMap.get(topicId).map {
                topic =>
                    mp(RequestTopicStoryFeed(topicId, page)).onSuccess {
                        case RequestTopicStoryFeedResponse(_, Right(feed)) =>
                            channel ! ReadTopicFeedResponse(msg, Right(new TopicStoryFeed(topic, feed)))
                    }
            }
    }

}
