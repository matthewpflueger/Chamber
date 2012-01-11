package com.echoed.chamber.services.echoeduser

import com.echoed.chamber.services.twitter.TwitterService
import com.echoed.chamber.dao.views.{ClosetDao,FeedDao}
import org.slf4j.LoggerFactory
import scalaz._
import Scalaz._
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.EchoedFriend
import scala.collection.JavaConversions._
import com.echoed.chamber.services.facebook.{GetFriendsResponse, FacebookService}
import akka.dispatch.Future
import java.util.{Date, ArrayList}
import com.echoed.chamber.dao._
import com.echoed.chamber.domain.views.{EchoFull, EchoViewDetail, EchoView,EchoViewPublic}
import scala.collection.mutable.{Map => MMap, ListBuffer => MList}
import com.echoed.chamber.services._
import akka.actor._


class EchoedUserServiceActor(
        var echoedUser: EchoedUser,
        echoedUserDao: EchoedUserDao,
        closetDao: ClosetDao,
        echoedFriendDao: EchoedFriendDao,
        feedDao: FeedDao,
        echoPossibilityDao: EchoPossibilityDao,
        retailerSettingsDao: RetailerSettingsDao,
        echoDao: EchoDao,
        var facebookService: Option[FacebookService] = None,
        var twitterService: Option[TwitterService] = None) extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[EchoedUserServiceActor])


    private def createEchoedFriends(echoedUsers: List[EchoedUser]) {
        logger.debug("Creating {} EchoedFriends for EchoedUser {}", echoedUsers.length, echoedUser.id)
        echoedUsers.foreach { eu =>
            echoedFriendDao.insertOrUpdate(new EchoedFriend(echoedUser, eu))
            echoedFriendDao.insertOrUpdate(new EchoedFriend(eu, echoedUser))
        }
        logger.debug("Saved {} EchoedFriends for {}", echoedUsers.length, echoedUser)
    }


    override def preStart() {
        //kick off a refresh of our friends...
        self ! FetchFacebookFriends()
        self ! FetchTwitterFollowers()
    }

    def receive = {
        case msg: GetEchoedUser => self.channel ! GetEchoedUserResponse(msg, Right(echoedUser))

        case msg: AssignTwitterService =>
            val me = self
            val channel = self.channel

            def error(e: Throwable) {
                channel ! AssignTwitterServiceResponse(msg, Left(new EchoedUserException("Cannot assign Twitter account", e)))
                logger.error("Unexpected error assigning TwitterService to EchoedUser {}", echoedUser.id)
            }

            def assign() {
                logger.debug("Assigning TwitterService to EchoedUser {}", this.echoedUser.id)
                msg.twitterService.assignEchoedUserId(echoedUser.id).onComplete(_.value.get.fold(
                    e => error(e),
                    tu => {
                        this.twitterService = Some(msg.twitterService)
                        this.echoedUser = this.echoedUser.assignTwitterUser(tu)
                        channel ! AssignTwitterServiceResponse(msg, Right(this.twitterService.get))
                        echoedUserDao.update(this.echoedUser)
                        me ! FetchTwitterFollowers()
                        logger.debug("Assigned TwitterUser {} to EchoedUser {}", tu.twitterId, this.echoedUser.id)
                    }))
            }

            try {
                logger.debug("Attempting to TwitterService to EchoedUser {}", this.echoedUser.id)
                msg.twitterService.getTwitterUser.onComplete(_.value.get.fold(
                    e => error(e),
                    twitterUser => Option(twitterUser.echoedUserId).cata(
                        echoedUserId =>
                            if (echoedUserId != this.echoedUser.id) {
                                channel ! AssignTwitterServiceResponse(msg, Left(new EchoedUserException("Twitter account already in use")))
                                logger.error(
                                    "Cannot assign Twitter account to EchoedUser {} because it is already in use by EchoedUser {}",
                                    echoedUser.id,
                                    echoedUserId)
                            } else {
                                assign()
                            },
                        {
                            assign()
                        })))
            } catch {
                case e => error(e)
            }


        case msg: AssignFacebookService =>
            val me = self
            val channel = self.channel

            def error(e: Throwable) {
                channel ! AssignFacebookServiceResponse(msg, Left(new EchoedUserException("Cannot assign Facebook account", e)))
                logger.error("Unexpected error assigning FacebookService to EchoedUser {}", echoedUser.id)
            }

            def assign() {
                msg.facebookService.assignEchoedUser(this.echoedUser).onComplete(_.value.get.fold(
                    e => error(e),
                    fu => {
                        logger.debug("Assigning FacebookService to EchoedUser {}", this.echoedUser.id)
                        this.facebookService = Some(msg.facebookService)
                        this.echoedUser = this.echoedUser.assignFacebookUser(fu)
                        channel ! AssignFacebookServiceResponse(msg, Right(this.facebookService.get))
                        echoedUserDao.update(this.echoedUser)
                        me ! FetchFacebookFriends()
                        logger.debug("Assigned FacebookUser {} to EchoedUser {}", fu.facebookId, this.echoedUser.id)
                    }))
            }

            try {
                logger.debug("Attempting to assign FacebookService to EchoedUser {}", echoedUser.id)
                msg.facebookService.getFacebookUser().onComplete(_.value.get.fold(
                    e => error(e),
                    facebookUser => Option(facebookUser.echoedUserId).cata(
                        echoedUserId =>
                            if (echoedUserId != this.echoedUser.id) {
                                channel ! AssignFacebookServiceResponse(
                                        msg,
                                        Left(new EchoedUserException("Facebook account already in use")))
                                logger.debug(
                                        "Cannot assign Facebook account to EchoedUser {} because it is already in use by EchoedUser {}",
                                        echoedUser.id,
                                        echoedUserId)
                            } else {
                                assign()
                            },
                        {
                            assign()
                        })))
            } catch {
                case e => error(e)
            }


        case msg @ EchoTo(_, echoPossibilityId, facebookMessage, echoToFacebook, twitterMessage, echoToTwitter) =>
            val me = self
            val channel = self.channel

            try {
                Option(echoPossibilityDao.findById(echoPossibilityId)).cata(
                    ep => {
                        Option(retailerSettingsDao.findByActiveOn(ep.retailerId, new Date)).cata(
                            retailerSettings => {
                                val echo = new Echo(ep, retailerSettings).echoed(retailerSettings)
                                echoDao.insert(echo)

                                Future { echoPossibilityDao.insertOrUpdate(ep.copy(echoId = echo.id, step = "echoed")) }

                                val requestList = MList[(ActorRef, Message)]()

                                if (echoToFacebook) requestList += ((me, EchoToFacebook(echo, facebookMessage)))
                                if (echoToTwitter) requestList += ((me, EchoToTwitter(echo, twitterMessage)))

                                val context = (channel, new EchoFull(echo, echoedUser), msg)
                                Actor.actorOf(classOf[ScatterGather]).start() ! Scatter(requestList.toList, Some(context))
                            },
                            {
                                channel ! EchoToResponse(msg, Left(EchoedUserException("Invalid echo possibility")))
                                logger.warn("Did not find RetailerSettings for Retailer {} for EchoPossibility {}", ep.retailerId, ep.id)
                            })
                    },
                    {
                        channel ! EchoToResponse(msg, Left(EchoedUserException("Invalid echo possibility")))
                        logger.warn("Did not find EchoPossibility {}", msg.echoPossibilityId)
                    })
            } catch {
                case e =>
                    channel ! EchoToResponse(msg, Left(EchoedUserException("Unexpected error processing %s" format msg, e)))
                    logger.error("Error processing {}", msg, e)
            }


        case msg @ ScatterResponse(
                Scatter(_, context: Some[Tuple3[Channel[EchoToResponse], EchoFull, EchoTo]], _, _),
                either) =>

            var (channel: Channel[EchoToResponse], echoFull: EchoFull, echoTo: EchoTo) = context.get

            def sendResponse(responses: List[Message]) {
                responses.foreach(message => message match {
                    case EchoToFacebookResponse(_, Right(fp)) => echoFull = echoFull.copy(facebookPost = fp)
                    case EchoToTwitterResponse(_, Right(ts)) => echoFull = echoFull.copy(twitterStatus = ts)
                    case unknown => logger.error("Error in responses from echo scatter {}", unknown)
                })
                channel ! EchoToResponse(echoTo, Right(echoFull))
            }

            try {
                logger.debug("Received response from echo scatter {}", msg)

                either.fold(
                    error => {
                        logger.error("Received error response: %s" format error, error)
                        sendResponse(error.responses)
                    },
                    responses => sendResponse(responses))
            } catch {
                case e => {
                    logger.error("Unexpected error in processing %s" format msg, e)
                    channel ! EchoToResponse(
                            echoTo,
                            Left(EchoedUserException("Could not echo %s" format echoTo, e)))
                }
            }


        case msg @ EchoToFacebook(echo, echoMessage) =>
            val channel = self.channel

            def error(e: Throwable) {
                channel ! EchoToFacebookResponse(msg, Left(EchoedUserException("Error echoing to Facebook", e)))
                logger.error("Unexpected error echoing %s" format echo, e)
            }

            try {
                val em = echoMessage.getOrElse("Checkout my recent purchase of %s" format echo.productName)
                facebookService.cata(
                    fs => fs.echo(echo, em).onComplete(_.value.get.fold(
                        e => error(e),
                        fp => {
                            val ec = echo.copy(facebookPostId = fp.id)
                            echoDao.updateFacebookPostId(ec)
                            channel ! EchoToFacebookResponse(msg.copy(echo = ec), Right(fp))
                            logger.debug("Successfully echoed {} to {}", ec, fp)
                        })),
                    {
                        channel ! EchoToFacebookResponse(msg, Left(EchoedUserException("Not associated to Facebook")))
                        logger.debug("Could not echo {} because user {} does not have a FacebookService", echo, echoedUser)
                    })
            } catch {
                case e => error(e)
            }


        case msg @ EchoToTwitter(echo, echoMessage) =>
            val channel = self.channel

            def error(e: Throwable) {
                channel ! EchoToTwitterResponse(msg, Left(EchoedUserException("Error echoing to Twitter", e)))
                logger.error("Unexpected error echoing %s" format echo, e)
            }

            try {
                val em = echoMessage.getOrElse("Checkout my recent purchase of %s" format echo.productName)
                twitterService.cata(
                    ts => ts.echo(echo, em).onComplete(_.value.get.fold(
                        e => error(e),
                        ts => {
                            val ec = echo.copy(twitterStatusId = ts.id)
                            echoDao.updateTwitterStatusId(ec)
                            channel ! EchoToTwitterResponse(msg.copy(echo = ec), Right(ts))
                            logger.debug("Successfully echoed {} to {}", ec, ts)
                        })),
                    {
                        channel ! EchoToTwitterResponse(msg, Left(EchoedUserException("Not associated to Twitter")))
                        logger.debug("Could not echo {} because user {} does not have a TwitterService", echo, echoedUser)
                    })
            } catch {
                case e => error(e)
            }


        case msg @ GetFriendExhibit(echoedFriendUserId) =>
            try {
                val echoedFriend = Option(echoedFriendDao.findFriendByEchoedUserId(echoedUser.id, echoedFriendUserId)).orNull
                self.channel ! GetFriendExhibitResponse(msg, Right(closetDao.findByEchoedUserId(echoedFriend.toEchoedUserId)))
            } catch {
                case e =>
                    self.channel ! GetFriendExhibitResponse(msg, Left(EchoedUserException("Cannot get friend exhibit", e)))
                    logger.error("Unexpected error when fetching friend exhibit for EchoedUser %s" format echoedUser.id, e)
            }

        case msg: GetPublicFeed =>
            try {
                logger.debug("Attempting to retrieve Public Feed for EchoedUser {}", echoedUser.id)
                val feed = asScalaBuffer(feedDao.getPublicFeed).toList
                self.channel ! GetPublicFeedResponse(msg, Right(feed))

            } catch {
                case e=>
                    self.channel ! GetPublicFeedResponse(msg, Left(new EchoedUserException("Cannot get public feed", e)))
                    logger.error("Unexpected error when fetching feed for EchoedUser %s" format echoedUser.id , e)
            }

        case msg: GetFeed =>
            try {
                logger.debug("Attempting to retrieve Feed for EchoedUser {}", echoedUser.id)
                val feed = feedDao.findByEchoedUserId(echoedUser.id)
                if (feed.echoes == null || (feed.echoes.size == 1 && feed.echoes.head.echoId == null)) {
                    self.channel ! GetFeedResponse(msg, Right(feed.copy(echoes = new ArrayList[EchoViewDetail])))
                } else {
                    self.channel ! GetFeedResponse(msg, Right(feed))
                }
            } catch {
                case e =>
                    self.channel ! GetFeedResponse(msg, Left(new EchoedUserException("Cannot get feed", e)))
                    logger.error("Unexpected error when fetching feed for EchoedUser %s" format echoedUser.id, e)
            }


        case msg: GetExhibit =>
            try {
                logger.debug("Fetching exhibit for EchoedUser {}", echoedUser.id)
                val credit = closetDao.totalCreditByEchoedUserId(echoedUser.id)
                val closet = closetDao.findByEchoedUserId(echoedUser.id)
                if (closet.echoes == null || (closet.echoes.size == 1 && closet.echoes.head.echoId == null)) {
                    self.channel ! GetExhibitResponse(msg, Right(closet.copy(
                            totalCredit = credit, echoes = new ArrayList[EchoView])))
                } else {
                    self.channel ! GetExhibitResponse(msg, Right(closet.copy(totalCredit = credit)))
                }
                logger.debug("Fetched exhibit with total credit {} for EchoedUser {}", credit, echoedUser.id)
            } catch {
                case e =>
                    self.channel ! GetExhibitResponse(msg, Left(new EchoedUserException("Cannot get exhibit", e)))
                    logger.error("Unexpected error when fetching exhibit for EchoedUser %s" format echoedUser.id, e)
            }


        case msg: GetEchoedFriends =>
            try {
                logger.debug("Loading EchoedFriends from database for EchoedUser {}", echoedUser.id)
                val echoedFriends = asScalaBuffer(echoedFriendDao.findByEchoedUserId(echoedUser.id)).toList
                self.channel ! GetEchoedFriendsResponse(msg, Right(echoedFriends))
                logger.debug("Found {} EchoedFriends in database for EchoedUser {}", echoedFriends.length, echoedUser.id)
            } catch {
                case e =>
                    self.channel ! GetEchoedFriendsResponse(msg, Left(EchoedUserException("Cannot get friends", e)))
                    logger.error("Unexpected error fetching friends for EchoedUser %s" format echoedUser.id, e)
            }


        case GetFriendsResponse(_, Left(e)) =>
            logger.error("Received error finding friends for EchoedUser %s" format echoedUser.id, e)


        case GetFriendsResponse(_, Right(ffs)) =>
            logger.debug("Fetched {} FacebookFriends for EchoedUser {}", ffs.length, echoedUser.id)
            val facebookEchoedUsers = ffs
                .map(ff => Option(echoedUserDao.findByFacebookId(ff.facebookId)))
                .filter(_.isDefined)
                .map(_.get)
            logger.debug("Found {} friends via Facebook for EchoedUser {}", facebookEchoedUsers.length, echoedUser.id)
            createEchoedFriends(facebookEchoedUsers)


        case msg: FetchFacebookFriends =>
            facebookService.collect{ case ac: ActorClient => ac.actorRef }.cata(
                _ ! '_fetchFacebookFriends,
                logger.debug("No FacebookService for EchoedUser {}", echoedUser.id)
            )


        case twitterFollowers: List[TwitterFollower] =>
            logger.debug("Fetched {} TwitterFollowers for EchoedUser {}", twitterFollowers.length, echoedUser.id)
            val twitterEchoedUsers = twitterFollowers
                .map(tf => Option(echoedUserDao.findByTwitterId(tf.twitterId)))
                .filter(_.isDefined)
                .map(_.get)
            logger.debug("Found {} friends via Twitter for EchoedUser {}", twitterEchoedUsers.length, echoedUser.id)
            createEchoedFriends(twitterEchoedUsers)


        case msg: FetchTwitterFollowers =>
            twitterService.collect { case ac: ActorClient => ac.actorRef }.cata(
                _ ! "getFollowers",
                logger.debug("No TwitterService for EchoedUser {}", echoedUser.id)
            )
    }
}
