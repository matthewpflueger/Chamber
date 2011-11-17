package com.echoed.chamber.services.twitter


import akka.actor.Actor
import collection.mutable.WeakHashMap
import reflect.BeanProperty
import akka.util.Duration
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import twitter4j.auth.{RequestToken,AccessToken}

class TwitterServiceLocatorActor extends Actor {

  private val logger = LoggerFactory.getLogger(classOf[TwitterServiceLocatorActor])

  @BeanProperty var twitterServiceCreator: TwitterServiceCreator = _

  private val cache = WeakHashMap[String, TwitterService]()
  private val idCache = WeakHashMap[String, TwitterService]() //hashmap of TwitterUserId : TwitterService

  def receive = {
    case ("none") =>{
      logger.debug("Creating New Twitter Service with No Token")
      val t = twitterServiceCreator.createTwitterService().await(Duration(10,TimeUnit.SECONDS)).get
      val requestToken: RequestToken = t.getRequestToken().get
      cache += ("requestToken:" + requestToken.getToken -> t)
      self.channel ! t
    }
    case ("requestToken",oAuthToken:String) =>{
      logger.debug("Locating Twitter Service with Request Token {}", oAuthToken)
      self.channel ! cache.getOrElse("requestToken:" + oAuthToken,null)
    }
    case ("accessToken",accessToken:AccessToken) =>{


      val accessTokenKey:String = "accessToken:" + accessToken.getToken

      val t = cache.getOrElse(accessTokenKey,{
        val twitterService = twitterServiceCreator.createTwitterServiceWithAccessToken(accessToken).await(Duration(10,TimeUnit.SECONDS)).get
        val twitterUserId = twitterService.getUser().get.id
        idCache +=(twitterUserId ->twitterService)
        cache += (accessTokenKey -> twitterService)
        twitterService
      })
      self.channel ! t
    }

    case ("id", twitterUserId:String) =>{
      val t = idCache.getOrElse(twitterUserId,{
        val twitterService = twitterServiceCreator.createTwitterServiceWithId(twitterUserId).await(Duration(10,TimeUnit.SECONDS)).get
        idCache += (twitterUserId -> twitterService)
        twitterService
      })
      self.channel ! t
    }
  }
}
