package com.echoed.chamber.services.echoeduser

import com.echoed.chamber.dao.views.{ClosetDao,FeedDao}
import org.slf4j.LoggerFactory
import scalaz._
import Scalaz._
import com.echoed.chamber.domain._
import com.echoed.chamber.domain.EchoedFriend
import scala.collection.JavaConversions._
import akka.dispatch.Future
import java.util.{Date, ArrayList}
import com.echoed.chamber.dao._
import scala.collection.mutable.{ListBuffer => MList}
import com.echoed.chamber.services._
import akka.actor._
import com.echoed.chamber.services.twitter._
import com.echoed.chamber.services.echoeduser.{Logout => L, LogoutResponse => LR, EchoToFacebookResponse => ETFR, EchoToFacebook => ETF}
import com.echoed.chamber.services.facebook._
import akka.util.Duration
import scala.collection.JavaConversions
import com.echoed.chamber.domain.views._
import com.echoed.chamber.domain.views.echoeduser.Profile
import com.echoed.chamber.dao.partner.PartnerSettingsDao


class EchoedUserServiceActor(
        var echoedUser: EchoedUser,
        echoedUserDao: EchoedUserDao,
        closetDao: ClosetDao,
        echoedFriendDao: EchoedFriendDao,
        feedDao: FeedDao,
        partnerSettingsDao: PartnerSettingsDao,
        echoDao: EchoDao,
        echoMetricsDao: EchoMetricsDao,
        facebookServiceLocator: FacebookServiceLocator,
        twitterServiceLocator: TwitterServiceLocator) extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[EchoedUserServiceActor])

    self.id = "EchoedUserService:%s" format echoedUser.id

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
        case msg: GetEchoedUser =>
            val channel: Channel[GetEchoedUserResponse] = self.channel
            channel ! GetEchoedUserResponse(msg, Right(echoedUser))


        case msg: GetProfile =>
            val channel: Channel[GetProfileResponse] = self.channel
            val totalCredit = closetDao.totalCreditByEchoedUserId(echoedUser.id)
            val totalVisits = closetDao.totalClicksByEchoedUserId(echoedUser.id)
            channel ! GetProfileResponse(msg, Right(new Profile(echoedUser, totalCredit, totalVisits)))

        case msg @ UpdateEchoedUserEmail(em) =>
            val channel: Channel[UpdateEchoedUserEmailResponse] = self.channel
            logger.debug("Updating Email for EchoedUser {} with {}", echoedUser, em )
            Option(echoedUserDao.findByEmail(em)).getOrElse(None) match {
                case None =>
                    logger.debug("Echoed User {} attempting to register existing email {}", echoedUser, em)
                    echoedUser = echoedUser.copy(email = em)
                    echoedUserDao.update(echoedUser)
                    channel ! UpdateEchoedUserEmailResponse(msg, Right(echoedUser))
                case eu =>
                    channel ! UpdateEchoedUserEmailResponse(msg, Left(EmailAlreadyExists(em)))
            }


        case msg @ UpdateEchoedUser(eu) =>
            val channel: Channel[UpdateEchoedUserResponse] = self.channel
            echoedUser = eu
            echoedUserDao.update(echoedUser)
            channel ! UpdateEchoedUserResponse(msg, Right(echoedUser))

        //Logout
        case msg @ L(echoedUserId) =>
            val channel: Channel[LR] = self.channel

            try {
                assert(echoedUser.id == echoedUserId)
                Option(echoedUser.facebookUserId).foreach(facebookServiceLocator.logout(_))
                Option(echoedUser.twitterUserId).foreach(twitterServiceLocator.logout(_))
                self.stop()
                logger.debug("Logged out Echoed user {}", echoedUser)
            } catch {
                case e =>
                    channel ! LR(msg, Left(EchoedUserException("Could not logout", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }

        case msg: AssignTwitterService =>
            import com.echoed.chamber.services.twitter.{AssignEchoedUserResponse => AEUR}

            val me = self
            val channel: Channel[AssignTwitterServiceResponse] = self.channel

            def error(e: Throwable) {
                channel ! AssignTwitterServiceResponse(msg, Left(new EchoedUserException("Cannot assign Twitter account", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            def assign() {
                logger.debug("Assigning TwitterService to {}", echoedUser)
                msg.twitterService.assignEchoedUser(echoedUser.id).onResult {
                    case AEUR(_, Left(e)) => error(e)
                    case AEUR(_, Right(twitterUser)) =>
                        echoedUser = echoedUser.assignTwitterUser(twitterUser)
                        channel ! AssignTwitterServiceResponse(msg, Right(msg.twitterService))
                        echoedUserDao.update(echoedUser)
                        me ! FetchTwitterFollowers()
                        logger.debug("Assigned {} to {}", twitterUser, echoedUser)
                }.onException { case e => error(e) }
            }

            try {
                logger.debug("Attempting to TwitterService to EchoedUser {}", this.echoedUser.id)
                msg.twitterService.getUser.onResult {
                    case GetUserResponse(_, Left(e)) => error(e)
                    case GetUserResponse(_, Right(twitterUser)) => Option(twitterUser.echoedUserId).cata(
                        echoedUserId =>
                            if (echoedUserId != echoedUser.id) {
                                channel ! AssignTwitterServiceResponse(
                                    msg, Left(new EchoedUserException("Twitter account already in use")))
                                logger.error(
                                    "Cannot assign Twitter account to EchoedUser {} because it is already in use by EchoedUser {}",
                                    echoedUser.id,
                                    echoedUserId)
                            } else {
                                assign()
                            },
                        {
                            assign()
                        })
                }.onException { case e => error(e) }
            } catch { case e => error(e) }


        case msg @ AssignFacebookService(facebookService) =>
            import com.echoed.chamber.services.facebook.{AssignEchoedUser => AEU, AssignEchoedUserResponse => AEUR}

            val me = self
            val channel: Channel[AssignFacebookServiceResponse] = self.channel

            def error(e: Throwable) {
                channel ! AssignFacebookServiceResponse(msg, Left(new EchoedUserException("Cannot assign Facebook account", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            def assign() {
                msg.facebookService.assignEchoedUser(this.echoedUser).onComplete(_.value.get.fold(
                    error(_),
                    _ match {
                        case AEUR(_, Left(e)) => error(e)
                        case AEUR(_, Right(fu)) =>
                            logger.debug("Assigning FacebookService to EchoedUser {}", echoedUser.id)
                            echoedUser = echoedUser.assignFacebookUser(fu)
                            channel ! AssignFacebookServiceResponse(msg, Right(msg.facebookService))
                            echoedUserDao.update(echoedUser)
                            me ! FetchFacebookFriends()
                            logger.debug("Assigned FacebookUser {} to EchoedUser {}", fu.facebookId, echoedUser.id)
                    }))
            }

            try {
                logger.debug("Attempting to assign FacebookService to EchoedUser {}", echoedUser.id)
                facebookService.getFacebookUser.onComplete(_.value.get.fold(
                    error(_),
                    _ match {
                        case GetFacebookUserResponse(_, Left(e)) => error(e)
                        case GetFacebookUserResponse(_, Right(facebookUser)) => Option(facebookUser.echoedUserId).cata(
                            echoedUserId =>
                                if (echoedUserId != echoedUser.id) {
                                    channel ! AssignFacebookServiceResponse(
                                            msg,
                                            Left(new EchoedUserException("Facebook account already in use")))
                                    logger.error(
                                            "Cannot assign Facebook account to EchoedUser {} because it is already in use by EchoedUser {}",
                                            echoedUser.id,
                                            echoedUserId)
                                } else {
                                    assign()
                                },
                            {
                                assign()
                            })
                    }))
            } catch { case e => error(e) }


        case msg @ EchoTo(_, echoPossibilityId, facebookMessage, echoToFacebook, twitterMessage, echoToTwitter) =>
            val me = self
            val channel: Channel[EchoToResponse] = self.channel

            try {
                //TODO finding by either id is to support the old integration method - should just use findById...
                Option(echoDao.findByIdOrEchoPossibilityId(echoPossibilityId)).cata(
                    ep => {
                        if(ep.isEchoed) {
                            logger.debug("Duplicate Echo: {}" , ep)
                            channel ! EchoToResponse(msg, Left(DuplicateEcho(ep,"Duplicate Echo")))
                        } else Option(partnerSettingsDao.findById(ep.partnerSettingsId)).cata(
                            partnerSettings => {

                                var echo = ep.copy(echoedUserId = echoedUser.id, step = ("%s,echoed" format ep.step).takeRight(254))
                                val echoMetrics = echoMetricsDao
                                        .findById(echo.echoMetricsId)
                                        .copy(echoedUserId = echoedUser.id)
                                        .echoed(partnerSettings)
                                echoMetricsDao.updateForEcho(echoMetrics)
                                echo = echo.copy(echoMetricsId = echoMetrics.id)
                                echoDao.updateForEcho(echo)
                                try{

                                    val requestList = MList[(ActorRef, Message)]()

                                    if (echoToFacebook) requestList += ((me, ETF(echo, facebookMessage)))
                                    if (echoToTwitter) requestList += ((me, EchoToTwitter(echo, twitterMessage, partnerSettings)))

                                    val context = (channel, new EchoFull(echo, echoedUser, partnerSettings), msg)
                                    Actor.actorOf(classOf[ScatterGather]).start() ! Scatter(
                                            requestList.toList,
                                            Some(context),
                                            Duration(20, "seconds"))

                                } catch {
                                    case e =>
                                        logger.debug("Duplicate Echo: {}", echo)
                                        channel ! EchoToResponse(msg, Left(DuplicateEcho(echo,"Duplicate Echo")))
                                }
                            },
                            {
                                channel ! EchoToResponse(msg, Left(EchoedUserException("Invalid echo possibility")))
                                logger.warn("Did not find PartnerSettings {} for Echo {}", ep.partnerSettingsId, ep.id)
                            })
                    },
                    {
                        channel ! EchoToResponse(msg, Left(EchoedUserException("Invalid echo possibility")))
                        logger.debug("Did not find Echo {}", msg.echoPossibilityId)
                    })
            } catch {
                case e =>
                    channel ! EchoToResponse(msg, Left(EchoedUserException("Unexpected error processing %s" format msg, e)))
                    logger.error("Error processing %s" format msg, e)
            }


        case msg @ ScatterResponse(
                Scatter(_, context: Some[Tuple3[Channel[EchoToResponse], EchoFull, EchoTo]], _, _),
                either) =>

            var (channel: Channel[EchoToResponse], echoFull: EchoFull, echoTo: EchoTo) = context.get

            def sendResponse(responses: List[Message]) {
                logger.debug("Scatter response size {}", responses.size)
                responses.foreach(message => message match {
                    case EchoToFacebookResponse(_, Right(fp)) =>
                        echoFull = echoFull.copy(facebookPost = fp)
                        logger.debug("Successfull Facebook echo {}", fp)
                    case EchoToTwitterResponse(_, Right(ts)) =>
                        echoFull = echoFull.copy(twitterStatus = ts)
                        logger.debug("Successfull Twitter echo {}", ts)
                    case unknown => logger.error("Error in responses from echo scatter {}", unknown)
                })
                channel ! EchoToResponse(echoTo, Right(echoFull))
                logger.debug("Sent successful Echo response {}", echoFull)
            }

            try {
                logger.debug("Received response from echo scatter:  channel {}", channel)
                logger.debug("Received response from echo scatter {}", msg)

                either.fold(
                    error => {
                        sendResponse(error.responses)
                        logger.error("Received error response: %s" format error, error)
                    },
                    responses => sendResponse(responses))
            } catch {
                case e => {
                    channel ! EchoToResponse(
                            echoTo,
                            Left(EchoedUserException("Could not echo", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
                }
            }

        //EchoToFacebook
        case msg @ ETF(echo, echoMessage) =>
            import com.echoed.chamber.services.facebook.{EchoToFacebookResponse => FETFR}
            val channel: Channel[ETFR] = self.channel

            def error(e: Throwable) {
                channel ! ETFR(msg, Left(EchoedUserException("Error posting to Facebook", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            try {
                val em = echoMessage.getOrElse("Checkout my recent purchase of %s" format echo.productName)
                Option(echoedUser.facebookUserId).cata(
                    fui => facebookServiceLocator.locateById(fui).onComplete(_.value.get.fold(
                        error(_),
                        _ match {
                            case LocateByIdResponse(_, Left(e)) => error(e)
                            case LocateByIdResponse(_, Right(fs)) =>
                                fs.echo(echo, em).onComplete(_.value.get.fold(
                                    error(_),
                                    _ match {
                                        case FETFR(_, Left(e)) => error(e)
                                        case FETFR(_, Right(fp)) =>
                                            val ec = echo.copy(facebookPostId = fp.id)
                                            echoDao.updateFacebookPostId(ec)
                                            channel ! ETFR(msg.copy(echo = ec), Right(fp))
                                            logger.debug("Successfully echoed {} to {}", ec, fp)
                                    }))
                        })),
                    {
                        channel ! ETFR(msg, Left(EchoedUserException("Not associated to Facebook")))
                        logger.debug("Could not echo {} because user {} does not have a FacebookService", echo, echoedUser)
                    })
            } catch {
                case e => error(e)
            }

        case msg @ PublishFacebookAction(action, obj, objUrl) =>
            val channel: Channel[PublishFacebookActionResponse] = self.channel

            def error(e: Throwable) {
                channel ! PublishFacebookActionResponse(msg, Left(EchoedUserException("Error publishin Facebook Action", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            try {
                Option(echoedUser.facebookUserId).cata(
                    fui => facebookServiceLocator.locateById(fui).onComplete(_.value.get.fold(
                        error(_),
                        _ match {
                            case LocateByIdResponse(_, Left(e)) => error(e)
                            case LocateByIdResponse(_, Right(fs)) =>
                                fs.publishAction(action, obj, objUrl).onComplete(_.value.get.fold(
                                    error(_),
                                    _ match {
                                        case PublishActionToFacebookResponse(_, Left(e)) => error(e)
                                        case PublishActionToFacebookResponse(_, Right(fa)) =>
                                            logger.debug("Successfully published Action")
                                    }))
                        })),
                    {
                        channel ! PublishFacebookActionResponse(msg, Left(EchoedUserException("Not Associated to Facebook")))
                        logger.debug("Could not publish action {} because user {} does not have a FacebookService", action)
                    })
            } catch {
                case e => error(e)
            }            


        case msg @ EchoToTwitter(echo, echoMessage, partnerSettings) =>
            val channel: Channel[EchoToTwitterResponse] = self.channel

            def error(e: Throwable) {
                channel ! EchoToTwitterResponse(msg, Left(EchoedUserException("Error tweeting", e)))
                logger.error("Unexpected error processing %s" format msg, e)
            }

            try {
                var em = echoMessage.getOrElse("Checkout my recent purchase of %s" format echo.productName)
                if(em.indexOf(partnerSettings.hashTag) == -1)
                    em = "%s %s" format (em.take(115 - (partnerSettings.hashTag.length() + 1)), partnerSettings.hashTag)
                Option(echoedUser.twitterUserId).cata( //facebookService.cata(
                    tui => twitterServiceLocator.getTwitterServiceWithId(tui).onComplete(_.value.get.fold(
                        e => error(e),
                        _ match {
                            case GetTwitterServiceWithIdResponse(_, Left(e)) => error(e)
                            case GetTwitterServiceWithIdResponse(_, Right(ts)) => ts.tweet(echo, em).onComplete(_.value.get.fold(
                                e => error(e),
                                _ match {
                                    case TweetResponse(_, Left(e)) => error(e)
                                    case TweetResponse(_, Right(twitterStatus)) =>
                                        val ec = echo.copy(twitterStatusId = twitterStatus.id)
                                        echoDao.updateTwitterStatusId(ec)
                                        channel ! EchoToTwitterResponse(msg.copy(echo = ec), Right(twitterStatus))
                                        logger.debug("Successfully tweeted {} to {}", ec, ts)
                                }
                            ))
                        })),
                    {
                        channel ! EchoToTwitterResponse(msg, Left(EchoedUserException("Not associated to Twitter")))
                        logger.debug("User {} not associated to Twitter to echo {}", echoedUser, echo)
                    })
            } catch {
                case e => error(e)
            }





        case msg @ GetFriendExhibit(echoedFriendUserId, page) =>
            val channel: Channel[GetFriendExhibitResponse] = self.channel

            try {
                val echoedFriend = echoedFriendDao.findFriendByEchoedUserId(echoedUser.id, echoedFriendUserId)
                val limit = 30;
                val start = msg.page * limit;

                val closet = Option(closetDao.findByEchoedUserId(echoedFriend.toEchoedUserId, start, limit))
                                .getOrElse(Closet(echoedFriend.toEchoedUserId, echoedUserDao.findById(echoedFriend.toEchoedUserId), null, 0))
                if (closet.echoes == null || (closet.echoes.size == 1 && closet.echoes.head.echoId == null)) {
                    channel ! GetFriendExhibitResponse(msg, Right(new FriendCloset(closet.copy(echoes = new ArrayList[EchoView]))))
                } else {
                    channel ! GetFriendExhibitResponse(msg, Right(new FriendCloset(closet)))
                }
            } catch {
                case e =>
                    channel ! GetFriendExhibitResponse(msg, Left(EchoedUserException("Cannot get friend exhibit", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }

        case msg: GetPublicFeed =>
            val channel: Channel[GetPublicFeedResponse] = self.channel
            val limit = 30;
            val start = msg.page * limit;
            try {
                logger.debug("Attempting to retrieve Public Feed for EchoedUser {}", echoedUser.id)
                //val echoes = JavaConversions.asJavaCollection(JavaConversions.asScalaBuffer(feedDao.getPublicFeed).map { new EchoViewPublic(_) })
                val echoes = asScalaBuffer(feedDao.getPublicFeed(start,limit)).toList
                val feed = new PublicFeed(echoes)
                channel ! GetPublicFeedResponse(msg, Right(feed))
            } catch {
                case e=>
                    channel ! GetPublicFeedResponse(msg, Left(new EchoedUserException("Cannot get public feed", e)))
                    logger.error("Unexpected error processing %s" format msg, e)
            }

        case msg: GetFeed =>
            val channel: Channel[GetFeedResponse] = self.channel

            try {
                logger.debug("Attempting to retrieve Feed for EchoedUser {}", echoedUser.id)
                val limit = 30;
                val start = msg.page * limit;
                val feed = Option(feedDao.findByEchoedUserId(echoedUser.id, start, limit)).getOrElse(Feed(echoedUser.id, echoedUser, null))
                if (feed.echoes == null || (feed.echoes.size == 1 && feed.echoes.head.echoId == null)) {
                    channel ! GetFeedResponse(msg, Right(feed.copy(echoes = new ArrayList[EchoViewDetail])))
                } else {
                    channel ! GetFeedResponse(msg, Right(feed))
                }
            } catch {
                case e =>
                    channel ! GetFeedResponse(msg, Left(new EchoedUserException("Cannot get feed", e)))
                    logger.error("Unexpected error when fetching feed for EchoedUser %s" format echoedUser.id, e)
            }

        case msg: GetPartnerFeed =>

            val channel: Channel[GetPartnerFeedResponse] = self.channel

            try{
                val partnerName = msg.partnerName
                logger.debug("Attempting to retrieve Partner Feed for Partner {} as EchoedUser {} ", partnerName, echoedUser.id)
                val limit = 30;
                val start = msg.page * limit;
                val echoes = asScalaBuffer(feedDao.getPartnerFeed(partnerName,start, limit)).toList
                val partnerFeed = new PublicFeed(echoes)
                channel ! GetPartnerFeedResponse(msg,Right(partnerFeed))
            } catch {
                case e =>
                    channel ! GetPartnerFeedResponse(msg, Left(new EchoedUserException("Canont get partner feed", e)))
                    logger.error("Unexpected erorr when fetching partner feed for EchoedUser %s" format echoedUser.id, e)
            }


        case msg: GetExhibit =>
            val channel: Channel[GetExhibitResponse] = self.channel

            try {
                logger.debug("Fetching exhibit for EchoedUser {}", echoedUser.id)
                val credit = closetDao.totalCreditByEchoedUserId(echoedUser.id)

                val limit = 30;
                val start = msg.page * limit;

                val closet = Option(closetDao.findByEchoedUserId(echoedUser.id, start, limit)).getOrElse(new Closet(echoedUser.id, echoedUser))
                if (closet.echoes == null || (closet.echoes.size == 1 && closet.echoes.head.echoId == null)) {
                    logger.debug("Echoed user {} has zero echoes", echoedUser.id)
                    channel ! GetExhibitResponse(msg, Right(new ClosetPersonal(closet.copy(
                            totalCredit = credit, echoes = new ArrayList[EchoView]))))
                } else {
                    logger.debug("Echoed user {} has {} echoes", echoedUser.id, closet.echoes.size)
                    channel ! GetExhibitResponse(msg, Right(new ClosetPersonal(closet.copy(totalCredit = credit))))
                }
                logger.debug("Fetched exhibit with total credit {} for EchoedUser {}", credit, echoedUser.id)
            } catch {
                case e =>
                    channel ! GetExhibitResponse(msg, Left(new EchoedUserException("Cannot get exhibit", e)))
                    logger.error("Unexpected error when fetching exhibit for EchoedUser %s" format echoedUser.id, e)
            }


        case msg: GetEchoedFriends =>
            val channel: Channel[GetEchoedFriendsResponse] = self.channel

            try {
                logger.debug("Loading EchoedFriends from database for EchoedUser {}", echoedUser.id)
                val echoedFriends = asScalaBuffer(echoedFriendDao.findByEchoedUserId(echoedUser.id)).toList
                channel ! GetEchoedFriendsResponse(msg, Right(echoedFriends))
                logger.debug("Found {} EchoedFriends in database for EchoedUser {}", echoedFriends.length, echoedUser.id)
            } catch {
                case e =>
                    channel ! GetEchoedFriendsResponse(msg, Left(EchoedUserException("Cannot get friends", e)))
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
            val me = self
            Option(echoedUser.facebookUserId).cata(
                facebookServiceLocator.locateById(_).onComplete(_.value.get.fold(
                    logger.error("Could not get FacebookService for %s" format echoedUser, _),
                    _ match {
                        case LocateByIdResponse(_, Left(e)) =>
                            logger.error("Could not get FacebookService for %s" format echoedUser, e)
                        case LocateByIdResponse(_, Right(ac: ActorClient)) =>
                            ac.actorRef.!('_fetchFacebookFriends)(me)
                    }
                )),
               logger.debug("No FacebookService for EchoedUser {}", echoedUser.id)
            )


        case GetFollowersResponse(_, Left(e)) =>
            logger.error("Received error fetching followers for EchoedUser %s" format echoedUser.id, e)


        case GetFollowersResponse(_, Right(twitterFollowers)) =>
            logger.debug("Fetched {} TwitterFollowers for EchoedUser {}", twitterFollowers.length, echoedUser.id)
            val twitterEchoedUsers = twitterFollowers
                .map(tf => Option(echoedUserDao.findByTwitterId(tf.twitterId)))
                .filter(_.isDefined)
                .map(_.get)
            logger.debug("Found {} friends via Twitter for EchoedUser {}", twitterEchoedUsers.length, echoedUser.id)
            createEchoedFriends(twitterEchoedUsers)


        case msg: FetchTwitterFollowers =>
            val me = self
            Option(echoedUser.twitterUserId).cata(
                twitterServiceLocator.getTwitterServiceWithId(_).onComplete(_.value.get.fold(
                    logger.error("Could not get TwitterService for %s" format echoedUser, _),
                    _ match {
                        case GetTwitterServiceWithIdResponse(_, Left(e)) =>
                            logger.error("Could not get TwitterService for %s" format echoedUser, e)
                        case GetTwitterServiceWithIdResponse(_, Right(ac: ActorClient)) =>
                            logger.debug("Got TwitterService {} for {}", ac.actorRef.id, echoedUser.id)
                            ac.actorRef.!(GetFollowers())(me)
                    }
                )),
                logger.debug("No TwitterService for EchoedUser {}", echoedUser.id)
            )
    }
}

