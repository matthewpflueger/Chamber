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
        cache += (accessTokenKey -> twitterService)
        twitterService
      })
      self.channel ! t
    }
  }
}