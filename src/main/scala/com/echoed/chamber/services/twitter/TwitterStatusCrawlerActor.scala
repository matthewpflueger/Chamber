package com.echoed.chamber.services.twitter

import org.slf4j.LoggerFactory
import scala.reflect.BeanProperty
import scalaz._
import Scalaz._
import com.echoed.chamber.services.ActorClient
import java.util.{Calendar, Date}
import com.echoed.chamber.dao.{FacebookCommentDao, FacebookLikeDao, TwitterStatusDao}
import akka.actor.{Scheduler, Actor}
import java.util.concurrent.{Future, TimeUnit}
import akka.dispatch.CompletableFuture


/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 1/27/12
 * Time: 10:52 AM
 * To change this template use File | Settings | File Templates.
 */

class TwitterStatusCrawlerActor extends Actor{
    
    private val logger = LoggerFactory.getLogger(classOf[TwitterStatusCrawlerActor])

    //@BeanProperty var twitterStatusDao: TwitterStatusDao
    @BeanProperty var interval: Long = 60000
    @BeanProperty var future: Option[CompletableFuture[GetStatusDataResponse]] = None


    override def preStart() {
        next
    }

    def next(response: GetStatusDataResponse){
        future.foreach(_.completeWithResult(response))
        next
    }

    def next {
        if(interval > 0) self ! 'next
    }
    
    def findTwitterStatusToCrawl = {
        val postedOnStartDate = {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_MONTH, - 8)
            cal.getTime
        }
        
        val postedOnEndDate = {
            val cal = Calendar.getInstance()
            cal.add(Calendar.HOUR, - 1)
            cal.getTime
        }
        None

    }

    protected def receive = {
        case _ =>
    }


}