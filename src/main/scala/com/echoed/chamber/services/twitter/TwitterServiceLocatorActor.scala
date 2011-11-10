package com.echoed.chamber.services.twitter


import akka.actor.Actor
import akka.dispatch.Future
import collection.mutable.WeakHashMap
import reflect.BeanProperty
import akka.util.Duration
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import twitter4j.auth.{RequestToken,AccessToken}

class TwitterServiceLocatorActor extends Actor {

  private val logger = LoggerFactory.getLogger(classOf[TwitterServiceLocatorActor])

  @BeanProperty var twitterServiceCreator: TwitterServiceCreator = null

  private val cache = WeakHashMap[String, TwitterService]()

  def receive = {
    case ("none") =>{
      logger.debug("Creating New Twitter Service {}")
      val t = twitterServiceCreator.createTwitterService().await(Duration(10,TimeUnit.SECONDS)).get
      val requestToken: RequestToken = t.getRequestToken().get
      cache += (requestToken.getToken -> t)
      self.channel ! t
    }
    case ("requestToken",oAuthToken:String) =>{
      self.channel ! cache.getOrElse(oAuthToken,null)
    }
    case ("accessToken",accessToken:String, accessTokenSecret: String) =>{
      val t = cache.getOrElse(accessToken,{
        val twitterService = twitterServiceCreator.createTwitterServiceWithAccessToken(accessToken,accessTokenSecret).await(Duration(10,TimeUnit.SECONDS)).get
        cache += (accessToken -> twitterService)
        twitterService
      })
      self.channel ! t
    }
  }
}