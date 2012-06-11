package com.echoed.chamber.services.feed

import reflect.BeanProperty
import scala.collection.JavaConversions._
import com.echoed.chamber.dao.views._
import com.echoed.chamber.domain.views.PublicFeed
import org.springframework.beans.factory.FactoryBean
import akka.actor.{ActorSystem, Props, ActorRef, Actor}
import akka.util.Timeout
import akka.util.duration._
import akka.event.Logging


class FeedServiceActor extends FactoryBean[ActorRef] {
    
    @BeanProperty var feedDao: FeedDao = _


    @BeanProperty var timeoutInSeconds = 20
    @BeanProperty var actorSystem: ActorSystem = _

    def getObjectType = classOf[ActorRef]

    def isSingleton = true

    def getObject = actorSystem.actorOf(Props(new Actor {

    implicit val timeout = Timeout(timeoutInSeconds seconds)
    private final val logger = Logging(context.system, this)

    def receive = {
        case msg @ GetPublicFeed(page: Int) =>
            val channel = context.sender
            val limit = 30;
            val start = msg.page * limit;

            try {
                logger.debug("Attempting to retrieve Public Feed ")
                val echoes = asScalaBuffer(feedDao.getPublicFeed(start,limit)).toList
                val feed = new PublicFeed(echoes)
                channel ! GetPublicFeedResponse(msg, Right(feed))
            } catch {
                case e=>
                    channel ! GetPublicFeedResponse(msg, Left(new FeedException("Cannot get public feed", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }
    }

    }), "FeedService")
}
