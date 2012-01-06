package com.echoed.chamber.services.echoeduser

import com.echoed.chamber.domain.views.{EchoViewDetail,EchoView}
import java.util.{ArrayList, List => JList}
import com.echoed.chamber.services.twitter.TwitterService
import com.echoed.chamber.dao.views.{ClosetDao,FeedDao}
import org.slf4j.LoggerFactory
import scalaz._
import Scalaz._
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.EchoedFriend
import com.echoed.chamber.dao.{EchoedFriendDao, EchoedUserDao}
import scala.collection.JavaConversions._
import com.echoed.chamber.services.facebook.{GetFriendsResponse, FacebookService}
import akka.dispatch.Future
import com.echoed.chamber.services.ActorClient
import akka.actor.Actor


class EchoedUserServiceActor(
        var echoedUser: EchoedUser,
        echoedUserDao: EchoedUserDao,
        closetDao: ClosetDao,
        echoedFriendDao: EchoedFriendDao,
        feedDao: FeedDao,
        var facebookService: FacebookService,
        var twitterService: TwitterService) extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[EchoedUserServiceActor])

    def this(echoedUser: EchoedUser, echoedUserDao: EchoedUserDao, closetDao: ClosetDao, echoedFriendDao: EchoedFriendDao,feedDao: FeedDao) =
            this(echoedUser, echoedUserDao, closetDao, echoedFriendDao, feedDao, null, null)
    def this(echoedUser: EchoedUser, echoedUserDao: EchoedUserDao, closetDao: ClosetDao, echoedFriendDao: EchoedFriendDao,feedDao: FeedDao, facebookService:FacebookService) =
            this(echoedUser,echoedUserDao, closetDao, echoedFriendDao,feedDao, facebookService, null)
    def this(echoedUser: EchoedUser, echoedUserDao: EchoedUserDao, closetDao: ClosetDao, echoedFriendDao: EchoedFriendDao,feedDao: FeedDao, twitterService:TwitterService) =
            this(echoedUser,echoedUserDao, closetDao, echoedFriendDao,feedDao, null, twitterService)


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
        self ! '_fetchFacebookFriends
        self ! '_fetchTwitterFollowers
    }

    def receive = {
        case "echoedUser" => self.channel ! echoedUser

        case msg:GetEchoedUser => self.channel ! GetEchoedUserResponse(msg,Right(echoedUser))

        case msg:AssignTwitterService =>
            val me = self
            val channel = self.channel

            def error(e: Throwable) {
                channel ! AssignTwitterServiceResponse(msg, Left(new EchoedUserException("Cannot assign Twitter account", e)))
                logger.error("Unexpected error assigning TwitterService to EchoedUser {}", echoedUser.id)
            }

            try {
                logger.debug("Assigning TwitterService to EchoedUser {}", echoedUser.id)
                msg.twitterService.getTwitterUser.onComplete(_.value.get.fold(
                    e => error(e),
                    twitterUser => Option(twitterUser.echoedUserId).cata(
                        echoedUserId => {
                            channel ! AssignTwitterServiceResponse(msg, Left(new EchoedUserException("Twitter account already in use")))
                            logger.error(
                                    "Cannot assign Twitter account to EchoedUser {} because it is already in use by EchoedUser {}",
                                    echoedUser.id,
                                    echoedUserId)
                        },
                        {
                            //FIXME really should be using Transactors for coordinated transactions (see API-22)...
                            msg.twitterService.assignEchoedUserId(echoedUser.id).onComplete(_.value.get.fold(
                                e => error(e),
                                tu => {
                                    this.twitterService = msg.twitterService
                                    this.echoedUser = this.echoedUser.assignTwitterUser(tu)
                                    channel ! AssignTwitterServiceResponse(msg, Right(this.twitterService))
                                    echoedUserDao.update(this.echoedUser)
                                    me ! '_fetchTwitterFollowers
                                    logger.debug("Assigned TwitterUser {} to EchoedUser {}", tu.twitterId, this.echoedUser.id)
                                }
                            ))
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

            try {
                logger.debug("Assigning FacebookService to EchoedUser {}", echoedUser.id)
                msg.facebookService.getFacebookUser().onComplete(_.value.get.fold(
                    e => error(e),
                    facebookUser => Option(facebookUser.echoedUserId).cata(
                        echoedUserId => {
                            channel ! AssignFacebookServiceResponse(msg, Left(new EchoedUserException("Facebook account already in use")))
                            logger.debug(
                                "Cannot assign Facebook account to EchoedUser {} because it is already in use by EchoedUser {}",
                                echoedUser.id,
                                echoedUserId)
                        },
                        {
                            msg.facebookService.assignEchoedUser(echoedUser).onComplete(_.value.get.fold(
                                e => error(e),
                                fu => {
                                    this.facebookService = msg.facebookService
                                    this.echoedUser = this.echoedUser.assignFacebookUser(fu)
                                    channel ! AssignFacebookServiceResponse(msg, Right(this.facebookService))
                                    echoedUserDao.update(echoedUser)
                                    me ! '_fetchFacebookFriends
                                    logger.debug("Assigned FacebookUser {} to EchoedUser {}", fu.facebookId, echoedUser.id)
                                }))
                        })))
            } catch {
                case e => error(e)
            }

        case("getTwitterFollowers") =>
            self.channel ! twitterService.getFollowers().get.asInstanceOf[Array[TwitterFollower]]

        case msg: EchoToFacebook =>
            val channel = self.channel
            facebookService.echo(msg.echo,msg.echoMessage).map[FacebookPost] {
                fp: FacebookPost =>
                    channel ! EchoToFacebookResponse(msg,Right(fp))
                    fp
            }

        case msg: EchoToTwitter =>
            val channel = self.channel
            twitterService.echo(msg.echo, msg.echoMessage).map[TwitterStatus] {
                tw: TwitterStatus =>
                    channel ! EchoToTwitterResponse(msg, Right(tw))
                    tw
            }

        case msg: GetFriendExhibit =>
            val echoedFriendUserId = msg.echoedFriendUserId
            val echoedUserId = echoedUser.id
            val echoedFriend = Option(echoedFriendDao.findFriendByEchoedUserId(echoedUserId,echoedFriendUserId)).getOrElse(null)
            self.channel ! GetFriendExhibitResponse(msg,Right(closetDao.findByEchoedUserId(echoedFriend.toEchoedUserId)))

        case msg: GetFeed =>
            logger.debug("Attempting to retrieve Feed for EchoedUserId {}", echoedUser.id)
            var resultSet = feedDao.findByEchoedUserId(echoedUser.id)
            if(resultSet.echoes.size == 1){
                if(resultSet.echoes.head.echoId == null){
                    resultSet = resultSet.copy(echoes= new ArrayList[EchoViewDetail])
                }
            }
            self.channel ! GetFeedResponse(msg, Right(resultSet))

        case msg: GetExhibit =>
            val channel = self.channel
            Future {
                logger.debug("Fetching exhibit for EchoedUser {}", echoedUser.id)
                var closet = closetDao.findByEchoedUserId(echoedUser.id)
                val credit = closetDao.totalCreditByEchoedUserId(echoedUser.id)
                if(closet.echoes.size == 1)
                    if(closet.echoes.head.echoId == null)
                        closet = closet.copy(echoes = new ArrayList[EchoView])

                channel ! GetExhibitResponse(msg, Right(closet.copy(totalCredit = credit)))
                logger.debug("Fetched exhibit with total credit {} for EchoedUser {}", credit, echoedUser.id)
            }

        case msg: GetEchoedFriends =>
            val channel = self.channel
            Future {
                logger.debug("Loading EchoedFriends from database for EchoedUser {}", echoedUser.id)
                val echoedFriends = asScalaBuffer(echoedFriendDao.findByEchoedUserId(echoedUser.id)).toList
                channel ! GetEchoedFriendsResponse(msg, Right(echoedFriends))
                logger.debug("Found {} EchoedFriends in database for EchoedUser", echoedFriends.length, echoedUser.id)
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

        case '_fetchFacebookFriends =>
            Option(facebookService).collect{ case ac: ActorClient => ac.actorRef }.cata(
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

        case '_fetchTwitterFollowers =>
            Option(twitterService).collect { case ac: ActorClient => ac.actorRef }.cata(
                _ ! "getFollowers",
                logger.debug("No TwitterService for EchoedUser {}", echoedUser.id)
            )
    }
}
