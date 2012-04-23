package com.echoed.chamber.services.feed

import reflect.BeanProperty
import scala.collection.JavaConversions._
import com.echoed.chamber.dao.views._
import akka.actor.{Channel, Actor}
import com.echoed.chamber.domain.views.PublicFeed
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 4/16/12
 * Time: 9:23 AM
 * To change this template use File | Settings | File Templates.
 */

class FeedServiceActor extends Actor{
    
    @BeanProperty var feedDao: FeedDao = _

    private final val logger = LoggerFactory.getLogger(classOf[FeedServiceActor])
    
    def receive = {
        case msg @ GetPublicFeed(page: Int) =>
            val channel: Channel[GetPublicFeedResponse] = self.channel
            val limit = 30;
            val start = msg.page * limit;
            try {
                logger.debug("Attempting to retrieve Public Feed ")
                //val echoes = JavaConversions.asJavaCollection(JavaConversions.asScalaBuffer(feedDao.getPublicFeed).map { new EchoViewPublic(_) })
                val echoes = asScalaBuffer(feedDao.getPublicFeed(start,limit)).toList
                val feed = new PublicFeed(echoes)
                channel ! GetPublicFeedResponse(msg, Right(feed))
            } catch {
                case e=>
                    channel ! GetPublicFeedResponse(msg, Left(new FeedException("Cannot get public feed", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }
    }

}
