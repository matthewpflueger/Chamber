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
                new TwitterServiceActor(null,requestToken,twitterAccess,twitterUserDao)).start)
    }
    case ("accessToken", accessToken: String, accessTokenSecret:String) =>{
      logger.debug("Creating New Twitter Service With Access Token {}")

        /*val twitterUser = for{
          aToken: AccessToken <- twitterAccess.getAccessToken(accessToken,accessTokenSecret)
          me:TwitterUser <- twitterAccess.getMe(aToken.getToken,aToken.getTokenSecret,aToken.getUserId)
          me.setAccessToken(accessToken)
          me.setAccessTokenSecret(accessTokenSecret)
          inserted : Int <- Future[Int] {twitterUserDao.insertOrUpdateTwitterUser(me)}
        } yield me*/

        //CREATE OR UPDATE TWITTERUSER DAO
        val aToken: AccessToken = twitterAccess.getAccessToken(accessToken,accessTokenSecret).get.asInstanceOf[AccessToken]
        var twitterUser: TwitterUser = twitterAccess.getMe(aToken.getToken,aToken.getTokenSecret,aToken.getUserId).get.asInstanceOf[TwitterUser]
        twitterUser.accessToken = accessToken
        twitterUser.accessTokenSecret = accessTokenSecret
        twitterUserDao.insertOrUpdateTwitterUser(twitterUser)
        self.channel ! new TwitterServiceActorClient(Actor.actorOf(
                           new TwitterServiceActor(twitterUser,null,twitterAccess,twitterUserDao)).start)
    }
  }

}