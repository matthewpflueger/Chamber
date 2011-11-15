package com.echoed.chamber.services.twitter


import akka.actor.Actor
import com.echoed.chamber.domain.TwitterUser
import reflect.BeanProperty
import com.echoed.chamber.dao.TwitterUserDao
import org.slf4j.LoggerFactory
import akka.dispatch.Future
import twitter4j.auth.{AccessToken,RequestToken}
import twitter4j.User



class TwitterServiceCreatorActor extends Actor{

  private val logger = LoggerFactory.getLogger(classOf[TwitterServiceCreatorActor])


  @BeanProperty var twitterAccess: TwitterAccess = null
  @BeanProperty var twitterUserDao: TwitterUserDao = null

  def receive = {
    case ("code") => {
      logger.debug("Creating New Twitter Service")
      val requestToken: RequestToken = twitterAccess.getRequestToken().get.asInstanceOf[RequestToken]
      self.channel ! new TwitterServiceActorClient(Actor.actorOf(
                new TwitterServiceActor(twitterAccess,twitterUserDao,requestToken)).start)
    }
    case ("accessToken", accessToken:AccessToken) =>{
      logger.debug("Creating New Twitter Service With Access Token {} for User Id", accessToken.getToken,accessToken.getUserId)
      var twitterUser: TwitterUser = twitterAccess.getUser(accessToken.getToken,accessToken.getTokenSecret,accessToken.getUserId).get.asInstanceOf[TwitterUser]
      twitterUser.accessToken = accessToken.getToken
      twitterUser.accessTokenSecret = accessToken.getTokenSecret
      twitterUserDao.insertOrUpdateTwitterUser(twitterUser)
      self.channel ! new TwitterServiceActorClient(Actor.actorOf(
                         new TwitterServiceActor(twitterAccess,twitterUserDao,twitterUser)).start)
    }
    case ("id", id:String) =>{
      logger.debug("Creating New Twitter Service With Id {}", id)
      var twitterUser: TwitterUser = twitterUserDao.selectTwitterUserWithId(id)
      self.channel ! new TwitterServiceActorClient(Actor.actorOf(
                          new TwitterServiceActor(twitterAccess,twitterUserDao,twitterUser)).start)
      }
  }

}