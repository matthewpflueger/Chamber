package com.echoed.chamber.services.facebook

import com.echoed.chamber.domain.FacebookUser
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import akka.dispatch.Future
import com.echoed.chamber.dao.{FacebookFriendDao, FacebookPostDao, FacebookUserDao, PartnerDao,  PartnerSettingsDao}
import akka.actor.{Channel, Actor}
import java.util.Properties


class FacebookServiceCreatorActor extends Actor {

    private val logger = LoggerFactory.getLogger(classOf[FacebookServiceCreatorActor])

    @BeanProperty var facebookAccess: FacebookAccess = _
    @BeanProperty var facebookUserDao: FacebookUserDao = _
    @BeanProperty var facebookPostDao: FacebookPostDao = _
    @BeanProperty var facebookFriendDao: FacebookFriendDao = _
    @BeanProperty var partnerSettingsDao: PartnerSettingsDao = _
    @BeanProperty var partnerDao: PartnerDao = _
    @BeanProperty var urlsProperties: Properties = _

    var echoClickUrl: String = _

    override def preStart() {
        echoClickUrl = urlsProperties.getProperty("echoClickUrl")
        assert(echoClickUrl != null)
    }

    def updateMe(me: FacebookUser) = {
        val facebookUser = Option(facebookUserDao.findByFacebookId(me.facebookId)) match {
            case Some(fu) =>
                logger.debug("Found Facebook User {}", me.facebookId)
                fu.copy(accessToken = me.accessToken,
                        name = me.name,
                        email = me.email,
                        facebookId = me.facebookId,
                        link = me.link,
                        gender = me.gender,
                        timezone = me.timezone,
                        locale = me.locale)
            case None =>
                logger.debug("No Facebook User {}", me.facebookId)
                me
        }

        logger.debug("Updating FacebookUser {} accessToken {}", facebookUser.id, facebookUser.accessToken)
        facebookUserDao.insertOrUpdate(facebookUser)
        facebookUser
    }

    def receive = {
        case msg @ CreateFromCode(code, queryString) =>
            val channel: Channel[CreateFromCodeResponse] = self.channel

            def error(e: Throwable) {
                channel ! CreateFromCodeResponse(msg, Left(FacebookException("Could not create Facebook service", e)))
                logger.error("Error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating FacebookService using code {}", code)
                facebookAccess.getMe(code, queryString).onComplete(_.value.get.fold(
                    error(_),
                    _ match {
                        case GetMeResponse(_, Left(e)) => error(e)
                        case GetMeResponse(_, Right(me)) =>
                            val facebookUser = updateMe(me)

                            channel ! CreateFromCodeResponse(msg, Right(new FacebookServiceActorClient(Actor.actorOf(
                                    new FacebookServiceActor(
                                            facebookUser,
                                            facebookAccess,
                                            facebookUserDao,
                                            facebookPostDao,
                                            facebookFriendDao,
                                            partnerDao,
                                            partnerSettingsDao,
                                            echoClickUrl)).start)))
                            logger.debug("Created FacebookService with user {}", facebookUser)
                    }))
            } catch { case e => error(e) }


        case msg @ CreateFromId(facebookUserId) =>
            val channel: Channel[CreateFromIdResponse] = self.channel

            try {
                logger.debug("Creating FacebookService using facebookUserId {}", facebookUserId)
                Option(facebookUserDao.findById(facebookUserId)) match {
                    case Some(facebookUser) =>
                        channel ! CreateFromIdResponse(msg, Right(
                            new FacebookServiceActorClient(Actor.actorOf(new FacebookServiceActor(
                                    facebookUser,
                                    facebookAccess,
                                    facebookUserDao,
                                    facebookPostDao,
                                    facebookFriendDao,
                                    partnerDao,
                                    partnerSettingsDao,
                                    echoClickUrl)).start)))
                        logger.debug("Created Facebook service {}", facebookUserId)
                    case None =>
                        channel ! CreateFromIdResponse(msg, Left(FacebookUserNotFound(facebookUserId)))
                        logger.debug("Did not find FacebookUser with id {}", facebookUserId)
                }
            } catch {
                case e =>
                    channel ! CreateFromIdResponse(msg, Left(FacebookException("Could not create Facebook service", e)))
                    logger.error("Error processing %s" format msg, e)
            }


        case msg @ CreateFromFacebookId(facebookId, accessToken) =>
            val channel: Channel[CreateFromFacebookIdResponse] = self.channel

            def error(e: Throwable) {
                channel ! CreateFromFacebookIdResponse(msg, Left(FacebookException("Could not create Facebook service", e)))
                logger.error("Error processing %s" format msg, e)
            }

            try {
                logger.debug("Creating FacebookService using facebookId {}", facebookId)
                facebookAccess.fetchMe(accessToken).onComplete(_.value.get.fold(
                    error(_),
                    _ match {
                        case FetchMeResponse(_, Left(e)) => error(e)
                        case FetchMeResponse(_, Right(me)) =>
                            val facebookUser = updateMe(me)
                            channel ! CreateFromFacebookIdResponse(msg, Right(
                                new FacebookServiceActorClient(Actor.actorOf(new FacebookServiceActor(
                                        facebookUser,
                                        facebookAccess,
                                        facebookUserDao,
                                        facebookPostDao,
                                        facebookFriendDao,
                                        partnerDao,
                                        partnerSettingsDao,
                                        echoClickUrl)).start)))
                            logger.debug("Created FacebookService from Facebook id {}", facebookId)
                    }))
            } catch { case e => error(e) }
    }
}
