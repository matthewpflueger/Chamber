package com.echoed.chamber.services.twitter


import com.echoed.chamber.domain.TwitterUser
import reflect.BeanProperty
import com.echoed.chamber.dao.{TwitterUserDao, TwitterStatusDao}
import org.slf4j.LoggerFactory
import twitter4j.auth.{AccessToken,RequestToken}
import scalaz._
import Scalaz._
import akka.actor.{Channel, Actor}

class TwitterServiceCreatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[TwitterServiceCreatorActor])

    @BeanProperty var twitterAccess: TwitterAccess = _
    @BeanProperty var twitterUserDao: TwitterUserDao = _
    @BeanProperty var twitterStatusDao: TwitterStatusDao = _


    def receive = {

        case msg @ CreateTwitterService(callbackUrl) =>
            val channel: Channel[CreateTwitterServiceResponse] = self.channel

            def error(e: Throwable) {
                channel ! CreateTwitterServiceResponse(msg, Left(TwitterException("Unexpected error creating Twitter service", e)))
                logger.error("Error creating Twitter service %s" format msg, e)
            }

            try {
                logger.debug("Creating new TwitterService with callback {}", callbackUrl)
                twitterAccess.getRequestToken(callbackUrl).onComplete(_.value.get.fold(
                    e => error(e),
                    _ match {
                        case FetchRequestTokenResponse(_, Right(requestToken)) =>
                            channel ! CreateTwitterServiceResponse(msg, Right(new TwitterServiceActorClient(Actor.actorOf(
                                new TwitterServiceActor(twitterAccess, twitterUserDao, twitterStatusDao, requestToken)).start)))
                        case FetchRequestTokenResponse(_, Left(e)) => error(e)
                    }))
            } catch { case e => error(e) }


        case msg @ CreateTwitterServiceWithAccessToken(accessToken) =>
            val channel: Channel[CreateTwitterServiceWithAccessTokenResponse] = self.channel

            def error(e: Throwable) {
                channel ! CreateTwitterServiceWithAccessTokenResponse(msg, Left(TwitterException("Unexpected error creating Twitter service", e)))
                logger.error("Error creating Twitter service %s" format msg, e)
            }

            try {
                logger.debug("Creating new Twitter service With access token {} for user {}", accessToken.getToken, accessToken.getUserId)
                twitterAccess.getUser(accessToken.getToken, accessToken.getTokenSecret, accessToken.getUserId).onComplete(_.value.get.fold(
                    e => error(e),
                    _ match {
                        case FetchUserResponse(_, Left(e)) => error(e)
                        case FetchUserResponse(_, Right(tu)) =>
                            logger.debug("Looking up twitter user with twitterId {}", accessToken.getUserId)
                            val twitterUser = Option(twitterUserDao.findByTwitterId(accessToken.getUserId.toString)).cata(
                                u => {
                                    logger.debug("Found TwitterUser {} with Twitter id {}", u, accessToken.getUserId)
                                    val t = u.copy(
                                        name = tu.name,
                                        profileImageUrl = tu.profileImageUrl,
                                        location = tu.location,
                                        timezone = tu.timezone,
                                        accessToken = accessToken.getToken,
                                        accessTokenSecret = accessToken.getTokenSecret)
                                    twitterUserDao.update(t)
                                    logger.debug("Successfully updated {}", t)
                                    t
                                },
                                {
                                    twitterUserDao.insert(tu)
                                    logger.debug("Successfully inserted {}", tu)
                                    tu
                                })

                            channel ! CreateTwitterServiceWithAccessTokenResponse(
                                    msg,
                                    Right(new TwitterServiceActorClient(Actor.actorOf(new TwitterServiceActor(
                                            twitterAccess,
                                            twitterUserDao,
                                            twitterStatusDao,
                                            twitterUser)).start)))
                    }
                ))
            } catch { case e => error(e) }


        case msg @ CreateTwitterServiceWithId(id) =>
            val channel: Channel[CreateTwitterServiceWithIdResponse] = self.channel

            def error(e: Throwable) {
                channel ! CreateTwitterServiceWithIdResponse(msg, Left(TwitterException("Unexpected error creating Twitter service", e)))
                logger.error("Error creating Twitter service %s" format msg, e)
            }

            try {
                logger.debug("Creating new TwitterService With id {}", id)
                Option(twitterUserDao.findById(id)).cata(
                    twitterUser => {
                        channel ! CreateTwitterServiceWithIdResponse(msg, Right(
                            new TwitterServiceActorClient(Actor.actorOf(
                                new TwitterServiceActor(twitterAccess, twitterUserDao, twitterStatusDao, twitterUser)).start)))
                        logger.debug("Created TwitterService with id {}", id)
                    },
                    {
                        channel ! CreateTwitterServiceWithIdResponse(
                                msg,
                                Left(TwitterUserNotFound(id)))
                        logger.debug("Twitter user with id {} not found", id)
                    })
            } catch { case e => error(e) }
    }

}
