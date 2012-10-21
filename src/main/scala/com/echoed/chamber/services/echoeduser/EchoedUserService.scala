package com.echoed.chamber.services.echoeduser

import com.echoed.chamber.dao.views.{ClosetDao,FeedDao}
import scalaz._
import Scalaz._
import scala.collection.JavaConversions._
import java.util.ArrayList
import com.echoed.chamber.dao._
import scala.collection.mutable.{ListBuffer => MList}
import com.echoed.chamber.services._
import akka.actor._
import akka.pattern._
import akka.util.duration._
import akka.actor.SupervisorStrategy.Stop
import com.echoed.chamber.dao.partner.{PartnerDao, PartnerSettingsDao}
import org.springframework.transaction.support.TransactionTemplate
import java.util.Date
import com.echoed.util.{Encrypter, UUID}
import akka.util.Timeout
import scala.collection.immutable.Stack
import com.echoed.chamber.services.scheduler.Hour
import com.echoed.chamber.domain._
import com.google.common.collect.HashMultimap
import com.echoed.chamber.services.state._
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.services.twitter.FetchFollowersResponse
import com.echoed.chamber.domain.views.EchoViewDetail
import com.echoed.chamber.domain.TwitterUser
import scala.Some
import com.echoed.chamber.domain.InvalidPassword
import com.echoed.chamber.domain.views.Feed
import com.echoed.chamber.domain.views.FriendCloset
import com.echoed.chamber.services.twitter.UpdateStatusResponse
import com.echoed.chamber.domain.EchoedUserSettings
import com.echoed.chamber.services.scheduler.ScheduleOnce
import com.echoed.chamber.domain.TwitterStatus
import com.echoed.chamber.services.state.ReadForFacebookUser
import com.echoed.chamber.domain.Notification
import com.echoed.chamber.domain.EchoedFriend
import com.echoed.chamber.services.twitter.GetFollowersResponse
import com.echoed.chamber.domain.FacebookUser
import com.echoed.chamber.services.state.FacebookUserNotFound
import akka.actor.Terminated
import com.echoed.chamber.services.facebook.FacebookAccessToken
import com.echoed.chamber.services.ScatterResponse
import com.echoed.chamber.services.state.ReadForCredentials
import scala.Left
import com.echoed.chamber.services.twitter.FetchFollowers
import com.echoed.chamber.domain.views.EchoView
import com.echoed.chamber.domain.views.FriendFeed
import com.echoed.chamber.services.facebook.FetchFriends
import com.echoed.chamber.services.state.TwitterUserNotFound
import akka.actor.OneForOneStrategy
import com.echoed.chamber.domain.views.StoryFull
import com.echoed.chamber.services.twitter.UpdateStatus
import com.echoed.chamber.domain.FacebookPost
import com.echoed.chamber.services.facebook.Post
import com.echoed.chamber.services.facebook.PostResponse
import com.echoed.chamber.services.facebook.PublishAction
import com.echoed.chamber.domain.views.EchoFull
import com.echoed.chamber.domain.views.ClosetPersonal
import com.echoed.chamber.domain.views.Closet
import com.echoed.chamber.services.email.SendEmail
import com.echoed.chamber.services.state.ReadForCredentialsResponse
import scala.Right
import com.echoed.chamber.services.facebook.FetchFriendsResponse
import com.echoed.chamber.services.Scatter
import com.echoed.chamber.services.state.ReadForTwitterUser
import com.echoed.chamber.services.state.ReadForTwitterUserResponse
import com.echoed.chamber.services.state.ReadForFacebookUserResponse
import com.echoed.chamber.services.feed.GetUserPublicStoryFeed
import com.echoed.chamber.services.feed.GetUserPublicStoryFeedResponse
import com.echoed.chamber.domain.views.EchoedUserStoryFeed
import com.echoed.chamber.domain.public.EchoedUserPublic


class EchoedUserService(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        initMessage: Message,
        storyServiceCreator: (ActorContext, Message, EchoedUser) => ActorRef,
        echoedUserDao: EchoedUserDao,
        closetDao: ClosetDao,
        echoedFriendDao: EchoedFriendDao,
        feedDao: FeedDao,
        partnerSettingsDao: PartnerSettingsDao,
        echoDao: EchoDao,
        echoMetricsDao: EchoMetricsDao,
        partnerDao: PartnerDao,
        facebookFriendDao: FacebookFriendDao,
        twitterFollowerDao: TwitterFollowerDao,
        facebookPostDao: FacebookPostDao,
        twitterStatusDao: TwitterStatusDao,
        transactionTemplate: TransactionTemplate,
        storyGraphUrl: String,
        echoClickUrl: String,
        encrypter: Encrypter,
        implicit val timeout: Timeout = Timeout(20000)) extends OnlineOfflineService {


    private var echoedUser: EchoedUser = _
    private var echoedUserSettings: EchoedUserSettings = _
    private var facebookUser: Option[FacebookUser] = None
    private var twitterUser: Option[TwitterUser] = None
    private var notifications = Stack[Notification]()
    private var followingUsers = List[Follower]()
    private var followedByUsers = List[Follower]()

    private val activeStories = HashMultimap.create[Identifiable, ActorRef]()

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: Exception ⇒ Stop
    }


    private def createEchoedFriends(echoedUsers: List[EchoedUser]) {
        log.debug("Creating {} EchoedFriends for EchoedUser {}", echoedUsers.length, echoedUser.id)
        echoedUsers.foreach { eu =>
            echoedFriendDao.insertOrUpdate(new EchoedFriend(echoedUser, eu))
            echoedFriendDao.insertOrUpdate(new EchoedFriend(eu, echoedUser))
        }
        log.debug("Saved {} EchoedFriends for {}", echoedUsers.length, echoedUser)
    }


    private def createCode(email: String, password: String) =
        encrypter.encrypt("""{ "email": "%s", "password": "%s" }""" format(email, password))


    private def verifyEmail: Unit =
        mp(SendEmail(
            echoedUser.email,
            "Email verification",
            "email_verification",
            Map("echoedUser" -> echoedUser, "code" -> createCode(echoedUser.email, echoedUser.password))))


    private def handleLoginWithFacebookUser(msg: LoginWithFacebookUser) {
        facebookUser = facebookUser
            .map {
                _.copy(
                    name = msg.facebookUser.name,
                    email = msg.facebookUser.email,
                    link = msg.facebookUser.link,
                    gender = msg.facebookUser.gender,
                    timezone = msg.facebookUser.timezone,
                    locale = msg.facebookUser.locale,
                    accessToken = msg.facebookUser.accessToken)
            }.orElse {
                echoedUser = echoedUser.assignFacebookUser(msg.facebookUser)
                Some(msg.facebookUser.copy(echoedUserId = echoedUser.id))
            }.map { fu =>
                mp.tell(FetchFriends(FacebookAccessToken(fu.accessToken, Some(fu.facebookId)), fu.id), self)
                //hack to reset our posts to be crawled - really should send a message to FacebookPostCrawler to crawl our posts...
                facebookPostDao.resetPostsToCrawl(fu.id)
                fu
            }

        becomeOnlineAndRegister
        msg.correlationSender.foreach(_ ! LoginWithFacebookResponse(msg.correlation, Right(echoedUser)))
    }


    private def handleLoginWithTwitterUser(msg: LoginWithTwitterUser) {
        twitterUser = twitterUser
            .map{
                _.copy(
                    name = msg.twitterUser.name,
                    screenName = msg.twitterUser.screenName,
                    profileImageUrl = msg.twitterUser.profileImageUrl,
                    location = msg.twitterUser.location,
                    timezone = msg.twitterUser.timezone,
                    accessToken = msg.twitterUser.accessToken,
                    accessTokenSecret = msg.twitterUser.accessTokenSecret)
            }.orElse {
                echoedUser = echoedUser.assignTwitterUser(msg.twitterUser)
                Some(msg.twitterUser.copy(echoedUserId = echoedUser.id))
            }.map { tu =>
                mp.tell(FetchFollowers(tu.accessToken, tu.accessTokenSecret, tu.id, tu.twitterId.toLong), self)
                tu
            }

        becomeOnlineAndRegister
        msg.correlationSender.foreach(_ ! LoginWithTwitterResponse(msg.correlation, Right(echoedUser)))
    }

    private def create(eu: EchoedUser, fu: Option[FacebookUser] = None, tu: Option[TwitterUser] = None) {
        echoedUser = eu
        facebookUser = fu.map(_.copy(echoedUserId = echoedUser.id))
        twitterUser = tu.map(_.copy(echoedUserId = echoedUser.id))
        facebookUser.map(echoedUser.assignFacebookUser(_))
        twitterUser.map(echoedUser.assignTwitterUser(_))
        echoedUserSettings = new EchoedUserSettings(echoedUser)
        ep(EchoedUserCreated(echoedUser, echoedUserSettings, facebookUser, twitterUser))
    }

    private def updated: Unit = ep(EchoedUserUpdated(echoedUser, echoedUserSettings, facebookUser, twitterUser))


    private def becomeOnlineAndRegister {
        becomeOnline
        context.parent ! RegisterEchoedUserService(echoedUser)
    }

    private def setStateAndRegister(euss: EchoedUserServiceState) {
        setState(euss)
        becomeOnlineAndRegister
    }

    private def setState(euss: EchoedUserServiceState) {
        echoedUser = euss.echoedUser
        echoedUserSettings = euss.echoedUserSettings
        twitterUser = euss.twitterUser
        facebookUser = euss.facebookUser
        notifications = euss.notifications
        followingUsers = euss.followingUsers
        followedByUsers = euss.followedByUsers
    }

    override def preStart() {
        super.preStart()
        initMessage match {
            case LoginWithEmailOrScreenName(emailOrScreenName, _, _) => mp.tell(ReadForEmailOrScreenName(emailOrScreenName), self)
            case LoginWithCredentials(credentials) => mp.tell(ReadForCredentials(credentials), self)
            case LoginWithFacebookUser(facebookUser, _, _) => mp.tell(ReadForFacebookUser(facebookUser), self)
            case LoginWithTwitterUser(twitterUser, _, _) => mp.tell(ReadForTwitterUser(twitterUser), self)
            case msg: RegisterLogin => //handled in init as we need to capture the sender
        }
    }


    def init = {
        case msg @ RegisterLogin(name, email, screenName, _, None) if (msg == initMessage) =>
            mp.tell(QueryUnique(new EchoedUser(name, email, screenName), msg, Option(sender)), self)

        case QueryUniqueResponse(QueryUnique(_, msg: RegisterLogin, channel), Left(e)) if (msg == initMessage) =>
            channel.get ! RegisterLoginResponse(msg, Left(InvalidRegistration(e.asErrors())))
            self ! PoisonPill

        case QueryUniqueResponse(QueryUnique(_, msg: RegisterLogin, channel), Right(true)) if (msg == initMessage) =>
            try {
                create(new EchoedUser(msg.name, msg.email, msg.screenName).createPassword(msg.password))
                channel.get ! RegisterLoginResponse(msg, Right(echoedUser))
                becomeOnlineAndRegister
                verifyEmail
            } catch {
                case e: InvalidPassword =>
                    channel.get ! RegisterLoginResponse(msg, Left(InvalidCredentials("Invalid password")))
                    self ! PoisonPill
            }

        case msg @ ReadForEmailOrScreenNameResponse(_, Left(_)) =>
            initMessage match {
                case LoginWithEmailOrScreenName(_, msg @ LoginWithEmailPassword(_, _), channel) =>
                    channel.get ! LoginWithEmailPasswordResponse(msg, Left(InvalidCredentials()))
                case LoginWithEmailOrScreenName(_, msg @ ResetLogin(_), channel) =>
                    channel.get ! ResetLoginResponse(msg, Left(InvalidCredentials()))
            }
            self ! PoisonPill


        case msg @ ReadForEmailOrScreenNameResponse(_, Right(euss)) => setStateAndRegister(euss)

        case msg @ ReadForCredentialsResponse(_, Right(euss)) => setStateAndRegister(euss)

        case msg @ ReadForFacebookUserResponse(_, Left(FacebookUserNotFound(fu, _))) =>
            create(new EchoedUser(fu), fu = Option(fu))
            handleLoginWithFacebookUser(initMessage.asInstanceOf[LoginWithFacebookUser])

        case msg @ ReadForFacebookUserResponse(_, Right(euss)) =>
            setState(euss)
            handleLoginWithFacebookUser(initMessage.asInstanceOf[LoginWithFacebookUser])
            updated

        case msg @ ReadForTwitterUserResponse(_, Left(TwitterUserNotFound(tu, _))) =>
            create(new EchoedUser(tu), tu = Option(tu))
            handleLoginWithTwitterUser(initMessage.asInstanceOf[LoginWithTwitterUser])

        case msg @ ReadForTwitterUserResponse(_, Right(euss)) =>
            setState(euss)
            handleLoginWithTwitterUser(initMessage.asInstanceOf[LoginWithTwitterUser])
            updated
    }


    def online = {
        case Terminated(ref) => activeStories.values.removeAll(activeStories.values.filter(_ == ref))

        case msg: LoginWithEmailPassword with Correlated[EchoedUserMessage] => msg.correlation match {
            case m: VerifyEmail =>
                log.debug("In verify email with {}", m)
                echoedUser = echoedUser.copy(emailVerified = true)
                updated
            case m: LoginWithCode =>
                msg.correlationSender.map(_ ! LoginWithCodeResponse(m, Right(echoedUser)))
        }

        case msg @ LoginWithEmailPassword(email, password) =>
            //compare the hashed version of the passwords here in case of password reset :(
            if (echoedUser.isCredentials(email, password) || echoedUser.email == email && echoedUser.password == password) {
                context.sender ! LoginWithEmailPasswordResponse(msg, Right(echoedUser))
            } else context.sender ! LoginWithEmailPasswordResponse(msg, Left(InvalidCredentials()))

        case msg @ ResetLogin(_) =>
            val password =
                if (echoedUser.hasPassword) echoedUser.password
                else {
                    val p = UUID()
                    echoedUser = echoedUser.createPassword(p)
                    updated
                    p
                }

            val code = encrypter.encrypt("""{ "email": "%s", "password": "%s" }""" format(echoedUser.email, password))
            mp(SendEmail(
                    echoedUser.email,
                    "Password reset",
                    "login_reset_email",
                    Map("echoedUser" -> echoedUser, "code" -> createCode(echoedUser.email, password))))
            sender ! ResetLoginResponse(msg, Right(code))


        case msg @ ResetPassword(_, password) =>
            echoedUser = echoedUser.createPassword(password)
            updated
            sender ! ResetPasswordResponse(msg, Right(echoedUser))


        case msg @ RegisterLogin(name, email, screenName, password, Some(eucc)) if (initMessage != msg && eucc.id == echoedUser.id) =>
            val channel = sender
            mp(QueryUnique(echoedUser.copy(name = name, email = email, screenName = screenName), msg, Some(sender))).onSuccess {
                case QueryUniqueResponse(_, Left(e)) =>
                    channel ! RegisterLoginResponse(msg, Left(InvalidRegistration(e.asErrors())))
                case QueryUniqueResponse(_, Right(true)) =>
                    val needsVerification = echoedUser.email != email
                    echoedUser = echoedUser.copy(name = name, email = email, screenName = screenName, emailVerified = needsVerification)
                    echoedUser = if (echoedUser.password != password) echoedUser.createPassword(password) else echoedUser
                    updated
                    channel ! RegisterLoginResponse(msg, Right(echoedUser))
                    if (needsVerification) verifyEmail
            }


        case msg @ LoginWithFacebookUser(fu, correlation, correlationSender) =>
            handleLoginWithFacebookUser(msg)
            updated


        case msg @ LoginWithTwitterUser(tu, correlation, correlationSender) =>
            handleLoginWithTwitterUser(msg)
            updated


        case msg: ReadSettings =>
            sender ! ReadSettingsResponse(msg, Right(echoedUserSettings))


        case msg @ NewSettings(_, eus) =>
            echoedUserSettings = echoedUserSettings.fromMap(eus)
            sender ! NewSettingsResponse(msg, Right(echoedUserSettings))
            updated


        case msg @ FetchFriendsResponse(_, Right(ffs)) =>
            ffs.foreach(facebookFriendDao.insertOrUpdate(_))
            log.debug("Fetched {} FacebookFriends for EchoedUser {}", ffs.length, echoedUser.id)
            val facebookEchoedUsers = ffs
                .map(ff => Option(echoedUserDao.findByFacebookId(ff.facebookId)))
                .filter(_.isDefined)
                .map(_.get)
            log.debug("Found {} friends via Facebook for EchoedUser {}", facebookEchoedUsers.length, echoedUser.id)
            createEchoedFriends(facebookEchoedUsers)


        case msg @ FetchFollowersResponse(_, Right(tfs)) =>
            tfs.foreach(twitterFollowerDao.insertOrUpdate(_))
            log.debug("Fetched {} TwitterFollowers for EchoedUser {}", tfs.length, echoedUser.id)
            val twitterEchoedUsers = tfs
                .map(tf => Option(echoedUserDao.findByTwitterId(tf.twitterId)))
                .filter(_.isDefined)
                .map(_.get)
            log.debug("Found {} friends via Twitter for EchoedUser {}", twitterEchoedUsers.length, echoedUser.id)
            createEchoedFriends(twitterEchoedUsers)


        case msg: GetEchoedUser =>
            val channel = context.sender
            channel ! GetEchoedUserResponse(msg, Right(echoedUser))


        case msg @ UpdateEchoedUserEmail(eucc, em) =>
            val channel = context.sender
            log.debug("Updating Email for EchoedUser {} with {}", echoedUser, em )
            Option(echoedUserDao.findByEmail(em)).getOrElse(None) match {
                case None =>
                    log.debug("Echoed User {} attempting to register existing email {}", echoedUser, em)
                    echoedUser = echoedUser.copy(email = em)
                    echoedUserDao.update(echoedUser)
                    channel ! UpdateEchoedUserEmailResponse(msg, Right(echoedUser))
                case eu =>
                    channel ! UpdateEchoedUserEmailResponse(msg, Left(EmailAlreadyExists(em)))
            }


        case msg @ PublishFacebookAction(eucc, action, obj, objUrl) =>
            facebookUser.foreach { fu =>
                mp(PublishAction(FacebookAccessToken(fu.accessToken, Some(fu.facebookId)), action, obj, objUrl))
            }


        case msg @ Logout(eucc) =>
            self ! PoisonPill
            log.debug("Logged out Echoed user {}", echoedUser)


        case msg @ GetEcho(eucc, echoId) =>
            Option(echoDao.findByIdAndEchoedUserId(echoId, echoedUser.id)).cata(
                echo => context.sender ! GetEchoResponse(msg, Right((echo, echoedUser, partnerDao.findById(echo.partnerId)))),
                context.sender ! GetEchoResponse(msg, Left(EchoNotFound(echoId))))


        case msg @ EchoTo(_, echoId, facebookMessage, echoToFacebook, twitterMessage, echoToTwitter) =>
            val me = self
            val channel = context.sender

            Option(echoDao.findById(echoId)).cata( ep =>
                if (ep.isEchoed) channel ! EchoToResponse(msg, Left(DuplicateEcho(ep,"Duplicate Echo")))
                else {
                    val partnerSettings = partnerSettingsDao.findById(ep.partnerSettingsId)
                    var echo = ep.copy(echoedUserId = echoedUser.id, step = ("%s,echoed" format ep.step).takeRight(254))
                    val echoMetrics = echoMetricsDao
                            .findById(echo.echoMetricsId)
                            .copy(echoedUserId = echoedUser.id)
                            .echoed(partnerSettings)
                    echoMetricsDao.updateForEcho(echoMetrics)
                    echo = echo.copy(echoMetricsId = echoMetrics.id)
                    echoDao.updateForEcho(echo)

                    val requestList = MList[(ActorRef, Message)]()

                    if (echoToFacebook) requestList += ((me, EchoToFacebook(echo, facebookMessage)))
                    if (echoToTwitter) requestList += ((me, EchoToTwitter(echo, twitterMessage, Option(partnerSettings.hashTag))))

                    val ctx = (channel, new EchoFull(echo, echoedUser, partnerSettings), msg)
                    context.actorOf(Props(classOf[ScatterGather])) ! Scatter(
                            requestList.toList,
                            Some(ctx),
                            20 seconds)
                },
                {
                    channel ! EchoToResponse(msg, Left(EchoNotFound(echoId)))
                    log.debug("Did not find Echo {}", echoId)
                })


        case msg @ ScatterResponse(
                Scatter(_, ctx: Some[(ActorRef, EchoFull, EchoTo)], _, _, _),
                either) =>

            var (channel: ActorRef, echoFull: EchoFull, echoTo: EchoTo) = ctx.get

            def sendResponse(responses: List[Message]) {
                log.debug("Scatter response size {}", responses.size)
                responses.foreach(message => message match {
                    case EchoToFacebookResponse(_, Right(fp)) =>
                        echoFull = echoFull.copy(facebookPost = fp)
                        log.debug("Successfull Facebook echo {}", fp)
                    case EchoToTwitterResponse(_, Right(ts)) =>
                        echoFull = echoFull.copy(twitterStatus = ts)
                        log.debug("Successfull Twitter echo {}", ts)
                })
                channel ! EchoToResponse(echoTo, Right(echoFull))
                log.debug("Sent successful Echo response {}", echoFull)
            }

            log.debug("Received response from echo scatter:  channel {}", channel)
            log.debug("Received response from echo scatter {}", msg)

            either.fold(log.error(_, "Received error responses"), sendResponse(_))


        case msg @ EchoToFacebook(echo, echoMessage) =>
            val fu = facebookUser.get
            val em = echoMessage.getOrElse("Checkout my recent purchase of %s" format echo.productName)
            log.debug("Creating new FacebookPost with message {} for {}", echo, em)
            val name = "%s from %s<center></center>" format(echo.productName, echo.brand)
            val caption = "%s<center></center>%s" format(echo.brand, echo.productName)
            var fp = new FacebookPost(
                    name,
                    em.take(254),
                    caption,
                    echo.imageUrl,
                    null,
                    fu.id,
                    echo.echoedUserId,
                    echo.id)
            fp = fp.copy(link = "%s/%s" format(echoClickUrl, fp.id))
            facebookPostDao.insert(fp)
            echoDao.updateFacebookPostId(echo.copy(facebookPostId = fp.id))

            context.sender ! EchoToFacebookResponse(msg, Right(fp))

            mp(Post(FacebookAccessToken(fu.accessToken, Option(fu.facebookId)), fp)).onSuccess {
                case PostResponse(_, Right(fp)) => facebookPostDao.updatePostedOn(fp.copy(postedOn = new Date))
            }


        case msg @ EchoToTwitter(echo, echoMessage, hashTag) =>
            val tu = twitterUser.get
            var em = echoMessage.getOrElse("Checkout my recent purchase of %s" format echo.productName)
            em = hashTag.map(t => if (!em.contains(t)) "%s %s" format (em.take(115 - (t.size + 1)), t) else em).get
            em = "%s %s/" format(em.take(115), echoClickUrl)
            var twitterStatus = new TwitterStatus(
                echo.id,
                echo.echoedUserId,
                em)
            em = em + twitterStatus.id
            twitterStatus = twitterStatus.copy(message = em)
            twitterStatusDao.insert(twitterStatus)
            echoDao.updateTwitterStatusId(echo.copy(twitterStatusId = twitterStatus.id))

            context.sender ! EchoToTwitterResponse(msg, Right(twitterStatus))

            mp(UpdateStatus(tu.accessToken, tu.accessTokenSecret, twitterStatus)).onSuccess {
                case UpdateStatusResponse(_, Right(ts)) => twitterStatusDao.updatePostedOn(ts.copy(postedOn = new Date))
            }


        case msg @ GetFriendExhibit(echoedFriendUserId, page) =>
            val channel = context.sender

            try {
                val echoedFriend = echoedFriendDao.findFriendByEchoedUserId(echoedUser.id, echoedFriendUserId)
                val limit = 30
                val start = msg.page * limit

                val closet = Option(closetDao.findByEchoedUserId(echoedFriend.toEchoedUserId, start, limit))
                                .getOrElse(Closet(echoedFriend.toEchoedUserId, echoedUserDao.findById(echoedFriend.toEchoedUserId), null, null, 0))
                if (closet.echoes == null || (closet.echoes.size == 1 && closet.echoes.head.echoId == null)) {
                    channel ! GetFriendExhibitResponse(msg, Right(new FriendCloset(closet.copy(echoes = new ArrayList[EchoView]))))
                } else {
                    channel ! GetFriendExhibitResponse(msg, Right(new FriendCloset(closet)))
                }
            } catch {
                case e =>
                    channel ! GetFriendExhibitResponse(msg, Left(EchoedUserException("Cannot get friend exhibit", e)))
                    log.error("Unexpected error processing {}, {}", msg, e)
            }


        case msg: GetFeed =>
            val channel = context.sender

            try {
                log.debug("Attempting to retrieve Feed for EchoedUser {}", echoedUser.id)
                val limit = 30
                val start = msg.page * limit
                val feed = Option(feedDao.findByEchoedUserId(echoedUser.id, start, limit)).getOrElse(Feed(echoedUser.id, echoedUser, null, null))
                if (feed.echoes == null || (feed.echoes.size == 1 && feed.echoes.head.echoId == null)) {
                    channel ! GetFeedResponse(msg, Right(feed.copy(echoes = new ArrayList[EchoViewDetail])))
                } else {
                    channel ! GetFeedResponse(msg, Right(feed))
                }
            } catch {
                case e =>
                    channel ! GetFeedResponse(msg, Left(new EchoedUserException("Cannot get feed", e)))
                    log.error("Unexpected error when fetching feed for EchoedUser {}, {}", echoedUser.id, e)
            }


        case msg: GetExhibit =>
            val channel = context.sender

            try {
                log.debug("Fetching exhibit for EchoedUser {}", echoedUser.id)
                val credit = closetDao.totalCreditByEchoedUserId(echoedUser.id)

                val limit = 30;
                val start = msg.page * limit

                val closet = Option(closetDao.findByEchoedUserId(echoedUser.id, start, limit)).getOrElse(new Closet(echoedUser.id, echoedUser))
                mp(GetUserPublicStoryFeed(echoedUser.id, msg.page)).onSuccess({
                    case GetUserPublicStoryFeedResponse(_ , Right(storyFeed)) =>
                        if (closet.echoes == null || (closet.echoes.size == 1 && closet.echoes.head.echoId == null)) {
                            log.debug("Echoed user {} has zero echoes", echoedUser.id)
                            channel ! GetExhibitResponse(msg, Right(new ClosetPersonal(closet.copy(
                                totalCredit = credit, stories = storyFeed.stories, echoes = new ArrayList[EchoView]))))
                        } else {
                            log.debug("Echoed user {} has {} echoes", echoedUser.id, closet.echoes.size)
                            channel ! GetExhibitResponse(msg, Right(new ClosetPersonal(closet.copy(totalCredit = credit, stories = storyFeed.stories))))
                        }
                        log.debug("Fetched exhibit with total credit {} for EchoedUser {}", credit, echoedUser.id)
                })
                (new ArrayList[StoryFull])
            } catch {
                case e =>
                    channel ! GetExhibitResponse(msg, Left(new EchoedUserException("Cannot get exhibit", e)))
                    log.error("Unexpected error when fetching exhibit for EchoedUser {}, {}", echoedUser.id, e)
            }


        case msg: GetEchoedFriends =>
            val channel = context.sender

            try {
                log.debug("Loading EchoedFriends from database for EchoedUser {}", echoedUser.id)
                val echoedFriends = asScalaBuffer(echoedFriendDao.findByEchoedUserId(echoedUser.id)).toList
                channel ! GetEchoedFriendsResponse(msg, Right(new FriendFeed(echoedFriends)))
                log.debug("Found {} EchoedFriends in database for EchoedUser {}", echoedFriends.length, echoedUser.id)
            } catch {
                case e =>
                    channel ! GetEchoedFriendsResponse(msg, Left(EchoedUserException("Cannot get friends", e)))
                    log.error("Unexpected error fetching friends for EchoedUser {}, {}", echoedUser.id, e)
            }


        case GetFollowersResponse(_, Right(twitterFollowers)) =>
            log.debug("Fetched {} TwitterFollowers for EchoedUser {}", twitterFollowers.length, echoedUser.id)
            val twitterEchoedUsers = twitterFollowers
                .map(tf => Option(echoedUserDao.findByTwitterId(tf.twitterId)))
                .filter(_.isDefined)
                .map(_.get)
            log.debug("Found {} friends via Twitter for EchoedUser {}", twitterEchoedUsers.length, echoedUser.id)
            createEchoedFriends(twitterEchoedUsers)


        case msg: FetchNotifications =>
            sender ! FetchNotificationsResponse(msg, Right(notifications.filterNot(_.hasRead)))


        case msg @ MarkNotificationsAsRead(_, ids) =>
            var marked = false
            notifications = notifications.map { n =>
                if (ids.contains(n.id)) {
                    val read = n.markAsRead
                    ep(NotificationUpdated(read))
                    marked = true
                    read
                } else n
            }

            sender ! MarkNotificationsAsReadResponse(msg, Right(marked))


        case RegisterNotification(_, n) =>
            notifications = notifications.push(n)
            ep(NotificationCreated(n))
            mp(ScheduleOnce(
                    Hour,
                    EmailNotifications(EchoedUserClientCredentials(echoedUser.id)),
                    Option(echoedUser.id)))


        case msg: EmailNotifications if (echoedUser.hasEmail && echoedUserSettings.receiveNotificationEmail) =>
            val toEmail = MList[Notification]()
            notifications = notifications.map { n =>
                if (n.canEmail) {
                    val emailed = n.markAsEmailed
                    toEmail += emailed
                    ep(NotificationUpdated(emailed))
                    emailed
                } else n
            }

            if (!toEmail.isEmpty)
                mp(SendEmail(
                    echoedUser.email,
                    "%s, you have new notifications on Echoed.com!" format echoedUser.name,
                    "email_notifications", Map( "notifications" -> toEmail.toList,
                                                "name" -> echoedUser.name)
                ))


        case msg: ListFollowingUsers => sender ! ListFollowingUsersResponse(msg, Right(followingUsers))
        case msg: ListFollowedByUsers => sender ! ListFollowedByUsersResponse(msg, Right(followedByUsers))

        case msg @ FollowUser(eucc, followerId) if (eucc.echoedUserId != followerId) =>
            mp.tell(AddFollower(EchoedUserClientCredentials(followerId), echoedUser, msg, Option(context.sender)), self)

        case AddFollowerResponse(AddFollower(_, _, msg, Some(s)), Right(eu)) =>
            followingUsers = Follower(eu.id, eu.name, Option(eu.facebookId), Option(eu.twitterId)) :: followingUsers
            s ! FollowUserResponse(msg, Right(true))
            ep(FollowerCreated(echoedUser.id, followingUsers.head))

        case msg @ AddFollower(eucc, eu, _, _) =>
            sender ! AddFollowerResponse(msg, Right(echoedUser))
            followedByUsers = Follower(eu.id, eu.name, Option(eu.facebookId), Option(eu.twitterId)) :: followedByUsers
            mp(RegisterNotification(eucc, new Notification(
                echoedUser.id,
                eu,
                "follower",
                Map(
                    "subject" -> eu.name,
                    "action" -> "is following",
                    "object" -> "you",
                    "followerId" -> eu.id))))

        case msg @ UnFollowUser(_, followingUserId) =>
            val (fu, fus) = followingUsers.partition(_.echoedUserId == followingUserId)
            followingUsers = fus
            fu.headOption.map { f =>
                mp(RemoveFollower(EchoedUserClientCredentials(f.echoedUserId), echoedUser))
                ep(FollowerDeleted(echoedUser.id, f))
            }
            sender ! UnFollowUserResponse(msg, Right(true))

        case msg @ RemoveFollower(_, eu) =>
            followedByUsers = followedByUsers.filterNot(_.echoedUserId == eu.id)
            sender ! RemoveFollowerResponse(msg, Right(true))

        case NotifyFollowers(_, notification) =>
            followedByUsers.foreach { f =>
                mp(RegisterNotification(
                    EchoedUserClientCredentials(f.echoedUserId),
                    new Notification(f.echoedUserId, echoedUser, notification.category, notification.value)))
            }

        case RegisterStory(story) =>
            activeStories.put(StoryId(story.id), sender)
            Option(story.echoId).map(e => activeStories.put(EchoId(e), sender))

        case msg @ InitStory(_, Some(storyId), _, _) => forwardToStory(msg, StoryId(storyId))
        case msg @ InitStory(_, _, Some(echoId), _) => forwardToStory(msg, EchoId(echoId))
        case msg @ InitStory(_, _, _, partnerId) => forwardToStory(msg, PartnerId(partnerId.getOrElse("Echoed")))

        case msg @ CreateStory(_, _, _, _, _, _, Some(echoId)) => forwardToStory(msg, EchoId(echoId))
        case msg: CreateStory =>
            //create a fresh actor for non-echo related stories
            context.watch(storyServiceCreator(context, msg, echoedUser)).forward(msg)

        case msg @ VoteStory(eucc, storyOwnerId, storyId, value) =>
            mp(new NewVote(new EchoedUserClientCredentials(storyOwnerId), echoedUser, storyId, value))
            sender ! VoteStoryResponse(msg, Right(true))
            if(value > 0) self ! PublishFacebookAction(eucc, "upvote", "story", storyGraphUrl + storyId)

        case msg @ CreateComment(eucc, storyOwnerId, storyId, chapterId, text, parentCommentId) =>
            val me = self
            mp(NewComment(
                    new EchoedUserClientCredentials(storyOwnerId),
                    echoedUser,
                    storyId,
                    chapterId,
                    text,
                    parentCommentId))
                .mapTo[NewCommentResponse]
                .map { ncr =>
                    me ! PublishFacebookAction(eucc, "comment_on", "story", storyGraphUrl + storyId)
                    CreateCommentResponse(msg, ncr.value)
                }.pipeTo(context.sender)

        case msg @ CreateChapter(eucc, storyId, _, _, _, _) =>
            forwardToStory(msg, StoryId(storyId))
            self ! PublishFacebookAction(eucc, "update", "story", storyGraphUrl + storyId)

        case msg @ GetUserFeed(eucc, page) =>
            val channel = sender
            mp(GetUserPublicStoryFeed(eucc.echoedUserId, page)).onSuccess {
                case GetUserPublicStoryFeedResponse(_, Right(feed)) =>
                    channel ! GetUserFeedResponse(msg, Right(new EchoedUserStoryFeed(new EchoedUserPublic(echoedUser), feed.stories, feed.nextPage)))
            }


        case msg: StoryIdentifiable with EchoedUserIdentifiable with Message =>
            forwardToStory(msg, StoryId(msg.storyId))
    }


    private def forwardToStory(msg: Message, identifiable: Identifiable) {
        activeStories.get(identifiable).headOption.cata(
            _.forward(msg),
            {
                val storyService = context.watch(storyServiceCreator(context, msg, echoedUser))
                storyService.forward(msg)
                activeStories.put(identifiable, storyService)
            })
    }
}

private case class StoryId(id: String) extends Identifiable
private case class EchoId(id: String) extends Identifiable
private case class PartnerId(id: String) extends Identifiable
