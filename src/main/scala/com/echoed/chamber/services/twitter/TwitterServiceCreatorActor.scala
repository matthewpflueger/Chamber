package com.echoed.chamber.services.twitter


import akka.actor.Actor
import com.echoed.chamber.domain.TwitterUser
import reflect.BeanProperty
import com.echoed.chamber.dao.{TwitterUserDao, TwitterStatusDao}
import org.slf4j.LoggerFactory
import twitter4j.auth.{AccessToken,RequestToken}

class TwitterServiceCreatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[TwitterServiceCreatorActor])

    @BeanProperty var twitterAccess: TwitterAccess = _
    @BeanProperty var twitterUserDao: TwitterUserDao = _
    @BeanProperty var twitterStatusDao: TwitterStatusDao = _


    def receive = {
        case ("code", callbackUrl: String) => {
            logger.debug("Creating New Twitter Service")
            val requestToken: RequestToken = twitterAccess.getRequestToken(callbackUrl).get.asInstanceOf[RequestToken]
            self.channel ! new TwitterServiceActorClient(Actor.actorOf(
                new TwitterServiceActor(twitterAccess, twitterUserDao, twitterStatusDao, requestToken)).start)
        }
        case ("accessToken", accessToken: AccessToken) => {
            val channel = self.channel
            logger.debug("Creating New Twitter Service With Access Token {} for User Id", accessToken.getToken, accessToken.getUserId)
            twitterAccess.getUser(accessToken.getToken, accessToken.getTokenSecret, accessToken.getUserId).map{
                twitterUser => 
                    logger.debug("Looking up twitter user with TwitterId {}", accessToken.getUserId)
                    val twUser: TwitterUser = twitterUserDao.findByTwitterId(accessToken.getUserId.toString)
                    var twUserFinal: TwitterUser = null
                    if (twUser != null){
                        logger.debug("Found TwitterUser {} with TwitterId ", twitterUser, accessToken.getUserId)
                        twUserFinal = twUser
                    }
                    else{
                        logger.debug("New Twitter User {}", twitterUser);
                        twitterUserDao.insert(twitterUser)
                        twUserFinal = twitterUser
                    }

                    channel ! new TwitterServiceActorClient(Actor.actorOf(
                        new TwitterServiceActor(twitterAccess, twitterUserDao, twitterStatusDao, twUserFinal)).start)
            }
        }

        case ("id", id: String) => {
            logger.debug("Creating New Twitter Service With Id {}", id)
            val twitterUser: TwitterUser = twitterUserDao.findById(id)
            self.channel ! new TwitterServiceActorClient(Actor.actorOf(
                new TwitterServiceActor(twitterAccess, twitterUserDao, twitterStatusDao, twitterUser)).start)
        }
    }

}
