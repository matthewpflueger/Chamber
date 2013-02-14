package com.echoed.chamber.services.echoeduser

import scalaz._
import Scalaz._
import scala.collection.JavaConversions._
import collection.mutable.{ListBuffer => MList}
import com.echoed.chamber.services._
import akka.actor._
import akka.pattern._
import scala.concurrent.duration._
import akka.actor.SupervisorStrategy.Stop
import java.util.Date
import com.echoed.util.{ScalaObjectMapper, Encrypter, UUID, DateUtils}
import akka.util.Timeout
import scala.collection.immutable.Stack
import com.echoed.chamber.services.scheduler.{Week, Hour, ScheduleOnce}
import com.echoed.chamber.domain._
import com.google.common.collect.HashMultimap
import com.echoed.chamber.services.state._
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.services.twitter.FetchFollowersResponse
import com.echoed.chamber.domain.TwitterUser
import scala.Some
import com.echoed.chamber.domain.InvalidPassword
import com.echoed.chamber.domain.EchoedUserSettings
import com.echoed.chamber.services.state.ReadForFacebookUser
import com.echoed.chamber.domain.Notification
import com.echoed.chamber.domain.FacebookUser
import com.echoed.chamber.domain.views.Feed
import views.context.{PersonalizedContext, UserContext, SelfContext}
import com.echoed.chamber.services.state.FacebookUserNotFound
import akka.actor.Terminated
import com.echoed.chamber.services.facebook.FacebookAccessToken
import com.echoed.chamber.services.state.ReadForCredentials
import scala.Left
import com.echoed.chamber.services.twitter.FetchFollowers
import com.echoed.chamber.services.facebook.FetchFriends
import com.echoed.chamber.services.state.TwitterUserNotFound
import akka.actor.OneForOneStrategy
import com.echoed.chamber.services.facebook.PublishAction
import com.echoed.chamber.services.email.SendEmail
import com.echoed.chamber.services.state.ReadForCredentialsResponse
import scala.Right
import com.echoed.chamber.services.facebook.FetchFriendsResponse
import com.echoed.chamber.services.state.ReadForTwitterUser
import com.echoed.chamber.services.state.ReadForTwitterUserResponse
import com.echoed.chamber.services.state.ReadForFacebookUserResponse
import com.echoed.chamber.domain.public.StoryPublic
import com.echoed.chamber.services.echoeduser.{EchoedUserClientCredentials => EUCC}
import com.echoed.chamber.services.partner.{RemovePartnerFollower, AddPartnerFollowerResponse, AddPartnerFollower, PartnerClientCredentials, ReadAllPartnerContent, ReadAllPartnerContentResponse}
import scala.concurrent.Future
import com.echoed.util.datastructure.ContentManager
import com.echoed.chamber.domain.views.content.{ContentDescription, PhotoContent, Content}
import com.echoed.chamber.domain.partner.Partner
import com.echoed.chamber.services.partner.{NotifyStoryUpdate => PNSU}


class EchoedUserService(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        initMessage: Message,
        storyServiceCreator: (ActorContext, Message, EchoedUser) => ActorRef,
        storyGraphUrl: String,
        echoClickUrl: String,
        encrypter: Encrypter,
        implicit val timeout: Timeout = Timeout(20000)) extends OnlineOfflineService {

    import context.dispatcher

    private var echoedUser:         EchoedUser = _
    private var echoedUserSettings: EchoedUserSettings = _
    private var facebookUser:       Option[FacebookUser] = None
    private var twitterUser:        Option[TwitterUser] = None
    private var notifications =     Stack[Notification]()
    private var followingUsers =    List[Follower]()
    private var followedByUsers =   List[Follower]()
    private var followingPartners = List[PartnerFollower]()

    private val followingContentManager =   new ContentManager(List(Story.storyContentDescription, PhotoContent.contentDescription))
    private val publicContentManager =      new ContentManager(List(Story.storyContentDescription, PhotoContent.contentDescription))
    private val privateContentManager =     new ContentManager(List(Story.storyContentDescription, PhotoContent.contentDescription))

    private var contentLoaded =         false
    private var customContentLoaded =   false

    private def becomeContentLoaded {
        contentLoaded = true
        unstashAll()
    }

    private def becomeCustomContentLoaded {
        customContentLoaded = true
        unstashAll()
    }

    private def userContext = {
        new UserContext(
            echoedUser,
            getStats ::: publicContentManager.getStats,
            publicContentManager.getHighlights,
            publicContentManager.getContentList)
    }



    private val activeStories = HashMultimap.create[Identifiable, ActorRef]()

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 0, withinTimeRange = 1.minute) {
        case _: Throwable ⇒ Stop
    }

    private def getStats = {
        var stats = List[Map[String, Any]]()
        stats = Map("name" -> "Sites Followed", "value" -> followingPartners.length,  "path" -> "following/partners") :: stats
        stats = Map("name" -> "Following",      "value" -> followingUsers.length,     "path" -> "following") :: stats
        stats = Map("name" -> "Followers",      "value" -> followedByUsers.length,    "path" -> "followers") :: stats
        stats
    }


    private def createCode(password: String) =
        encrypter.encrypt("""{ "password": "%s", "createdOn": "%s" }""" format(password, DateUtils.dateToLong(new Date)))


    private def createCredentials(fu: Option[FacebookUser] = None, tu: Option[TwitterUser] = None) =
        EUCC(
            echoedUser.id,
            Option(echoedUser.name),
            Option(echoedUser.email).orElse(fu.map(_.email)),
            Option(echoedUser.screenName).orElse(tu.map(_.screenName)),
            Option(echoedUser.facebookId).orElse(fu.map(_.facebookId)),
            Option(echoedUser.twitterId).orElse(tu.map(_.twitterId)),
            Option(echoedUser.password))


    private def verifyEmail: Unit =
        mp(SendEmail(
            echoedUser.email,
            "Email verification",
            "email_verification",
            Map("echoedUser" -> echoedUser, "code" -> createCode(echoedUser.password))))


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
//                facebookPostDao.resetPostsToCrawl(fu.id)
                fu
            }

        becomeOnlineAndRegister
        msg.correlationSender.foreach(_ ! LoginWithFacebookResponse(
                msg.correlation,
                Right(createCredentials(facebookUser))))
    }

    private def handleLoginWithTwitterUser(msg: LoginWithTwitterUser) {
        twitterUser = twitterUser
            .map {
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
        msg.correlationSender.foreach(_ ! LoginWithTwitterResponse(
                msg.correlation,
                Right(createCredentials(tu = twitterUser))))
    }

    private def create(eu: EchoedUser, fu: Option[FacebookUser] = None, tu: Option[TwitterUser] = None) {
        echoedUser =    eu
        facebookUser =  fu.map(_.copy(echoedUserId = echoedUser.id))
        twitterUser =   tu.map(_.copy(echoedUserId = echoedUser.id))
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

    private def getContent {
        mp(FindAllUserStories(echoedUser.id)).onSuccess {
            case FindAllUserStoriesResponse(_, Right(content)) =>
                val contentList = content.map { c => new StoryPublic(c.asStoryFull.get) }.toList
                self ! InitializeUserContentFeed(EchoedUserClientCredentials(echoedUser.id), contentList)
        }
    }

    private def getCustomFeed {
        val futureUserFeeds = for(fu <- followingUsers) yield {
            mp(ReadAllUserContent(EchoedUserClientCredentials(fu.echoedUserId)))
        }
        val futurePartnerFeeds = for(fp <- followingPartners) yield {
            mp(ReadAllPartnerContent(PartnerClientCredentials(fp.partnerId)))
        }
        val futures = futureUserFeeds ::: futurePartnerFeeds
        val futuresList = Future.sequence(futures)
        futuresList.onSuccess {
            case list =>
                var contentList = List[Content]()
                list.map {
                    case r @ ReadAllUserContentResponse(_, Right(content)) =>
                        contentList = contentList ::: content
                    case r @ ReadAllPartnerContentResponse(_, Right(content)) =>
                        contentList = contentList ::: content
                }
                self ! InitializeUserCustomFeed(EchoedUserClientCredentials(echoedUser.id), contentList)

        }
    }

    private def setStateAndRegister(euss: EchoedUserServiceState) {
        setState(euss)
        becomeOnlineAndRegister
    }

    private def setState(euss: EchoedUserServiceState) {
        echoedUser =          euss.echoedUser
        echoedUserSettings =  euss.echoedUserSettings
        twitterUser =         euss.twitterUser
        facebookUser =        euss.facebookUser
        notifications =       euss.notifications
        followingUsers =      euss.followingUsers
        followedByUsers =     euss.followedByUsers
        followingPartners =   euss.followingPartners
    }

    override def preStart() {
        super.preStart()
        initMessage match {
            case LoginWithCredentials(credentials, _, _) =>     mp.tell(ReadForCredentials(credentials), self)
            case LoginWithFacebookUser(facebookUser, _, _) =>   mp.tell(ReadForFacebookUser(facebookUser), self)
            case LoginWithTwitterUser(twitterUser, _, _) =>     mp.tell(ReadForTwitterUser(twitterUser), self)
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
                channel.get ! RegisterLoginResponse(msg, Right(createCredentials()))
                becomeOnlineAndRegister
                verifyEmail
            } catch {
                case e: InvalidPassword =>
                    channel.get ! RegisterLoginResponse(msg, Left(InvalidCredentials("Invalid password")))
                    self ! PoisonPill
            }

        case ReadForCredentialsResponse(_, Left(_)) =>
            (initMessage: @unchecked) match {
                case LoginWithCredentials(_, msg @ LoginWithPassword(_), channel) =>
                    channel.get ! LoginWithPasswordResponse(msg, Left(InvalidCredentials()))
                case LoginWithCredentials(_, msg @ ResetLogin(_), channel) =>
                    channel.get ! ResetLoginResponse(msg, Left(InvalidCredentials()))
                case msg: LoginWithCredentials =>
                    //this is here to catch staging credentials being transmitted to production :(
                    log.debug("Invalid credentials %s" format(msg))
            }
            self ! PoisonPill

        case ReadForCredentialsResponse(_, Right(euss)) => setStateAndRegister(euss)

        case ReadForFacebookUserResponse(_, Left(FacebookUserNotFound(fu, _))) =>
            create(new EchoedUser(fu), fu = Option(fu))
            handleLoginWithFacebookUser(initMessage.asInstanceOf[LoginWithFacebookUser])

        case ReadForFacebookUserResponse(_, Right(euss)) =>
            setState(euss)
            handleLoginWithFacebookUser(initMessage.asInstanceOf[LoginWithFacebookUser])
            updated

        case ReadForTwitterUserResponse(_, Left(TwitterUserNotFound(tu, _))) =>
            create(new EchoedUser(tu), tu = Option(tu))
            handleLoginWithTwitterUser(initMessage.asInstanceOf[LoginWithTwitterUser])

        case ReadForTwitterUserResponse(_, Right(euss)) =>
            setState(euss)
            handleLoginWithTwitterUser(initMessage.asInstanceOf[LoginWithTwitterUser])
            updated
    }

    def online = {

        case msg @ InitializeUserCustomFeed(_, content) =>
            content.map {
                case c: StoryPublic =>
                    if( c.echoedUser.id != echoedUser.id ) {
                        followingContentManager.updateContent(c)
                        c.extractImages.map { i => followingContentManager.updateContent(new PhotoContent(i, c)) }
                    }
                case _ =>
                    followingContentManager.updateContent(_)
            }
            becomeCustomContentLoaded

        case msg @ InitializeUserContentFeed(_, content) =>
            content.map {
                case c: StoryPublic =>
                    if(c.isPublished){
                        publicContentManager.updateContent(c.published)
                        c.extractImages.map { i => publicContentManager.updateContent(new PhotoContent(i, c)) }
                    }
                    privateContentManager.updateContent(c)
                    c.extractImages.map { i => privateContentManager.updateContent(new PhotoContent(i, c)) }

                case _ =>
                    publicContentManager.updateContent(_)
            }
            becomeContentLoaded


        case msg @ RequestOwnContent(_, page, _type) =>
            if(!contentLoaded) {
                stash()
                getContent
            } else {
                val content = privateContentManager.getContent(_type, page)
                val stats = getStats ::: privateContentManager.getStats
                val cf = new Feed(
                            new SelfContext(
                                echoedUser,
                                stats,
                                privateContentManager.getHighlights,
                                privateContentManager.getContentList
                            ),
                            content._1,
                            content._2)
                sender ! RequestOwnContentResponse(msg, Right(cf))
            }

        case msg @ RequestCustomUserFeed(_, page, _type) =>
            if(!customContentLoaded){
                stash()
                getCustomFeed
            } else {
                val content = followingContentManager.getContent(_type, page)
                val sf = new Feed(
                            new PersonalizedContext(
                                followingContentManager.getStats,
                                followingContentManager.getHighlights,
                                followingContentManager.getContentList
                            ),
                            content._1,
                            content._2)
                sender ! RequestCustomUserFeedResponse(msg, Right(sf))
            }

        case msg: ReadAllUserContent =>
            if(!contentLoaded) {
                stash()
                getContent
            } else {
                val content = publicContentManager.getAllContent
                sender ! ReadAllUserContentResponse(msg, Right(content))
            }

        case msg @ RequestUserContentFeed(eucc, page, _type) =>
            if(!contentLoaded) {
                stash()
                getContent
            } else {
                val content =   publicContentManager.getContent(_type, page)
                val sf =        new Feed(userContext, content._1, content._2)
                sender ! RequestUserContentFeedResponse(msg, Right(sf))
            }

        case Terminated(ref) => activeStories.values.removeAll(activeStories.values.filter(_ == ref))


        case msg @ VerifyEmail(_, code) =>
            val map = ScalaObjectMapper(encrypter.decrypt(code), classOf[Map[String, String]])
            map.get("password").filter(_ == echoedUser.password).foreach { _ =>
                echoedUser = echoedUser.copy(emailVerified = true)
                updated
                sender ! VerifyEmailResponse(msg, Right(createCredentials()))
            }


        case msg @ ResetPassword(_, code, password) =>
            val map = ScalaObjectMapper(encrypter.decrypt(code), classOf[Map[String, String]])
            map.get("password").filter(_ == echoedUser.password).foreach { _ =>
                echoedUser = echoedUser.createPassword(password)
                updated
                sender ! ResetPasswordResponse(msg, Right(createCredentials()))
            }


        case msg @ LoginWithPassword(eucc) if (eucc.password.isDefined) =>
            if (echoedUser.isCredentials(eucc.id, eucc.password.get)) {
                sender ! LoginWithPasswordResponse(msg, Right(createCredentials()))
            } else sender ! LoginWithPasswordResponse(msg, Left(InvalidCredentials()))


        case msg @ ResetLogin(_) =>
            val password =
                if (echoedUser.hasPassword) echoedUser.password
                else {
                    echoedUser = echoedUser.createPassword(UUID())
                    updated
                    echoedUser.password
                }

            mp(SendEmail(
                    echoedUser.email,
                    "Password reset",
                    "login_reset_email",
                    Map("echoedUser" -> echoedUser, "code" -> createCode(password))))
            sender ! ResetLoginResponse(msg, Right(true))


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
                    channel ! RegisterLoginResponse(msg, Right(createCredentials()))
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
            mp.tell(QueryEchoedUsersByFacebookId(createCredentials(), ffs.map(_.facebookId)), self)

        case QueryEchoedUsersByFacebookIdResponse(_, Right(list)) =>
            val set = followingUsers.map(_.echoedUserId).toSet
            list.filterNot(set.contains(_)).foreach(self ! FollowUser(createCredentials(), _))


        case msg @ FetchFollowersResponse(_, Right(tfs)) =>
            mp.tell(QueryEchoedUsersByTwitterId(createCredentials(), tfs.map(_.twitterId)), self)

        case QueryEchoedUsersByTwitterIdResponse(_, Right(list)) =>
            val set = followingUsers.map(_.echoedUserId).toSet
            list.filterNot(set.contains(_)).foreach(self ! FollowUser(createCredentials(), _))


        case msg: GetEchoedUser =>
            val channel = context.sender
            channel ! GetEchoedUserResponse(msg, Right(echoedUser))


        case msg @ PublishFacebookAction(eucc, action, obj, objUrl) =>
            facebookUser.foreach { fu =>
                mp(PublishAction(FacebookAccessToken(fu.accessToken, Some(fu.facebookId)), action, obj, objUrl))
            }

        case msg @ Logout(eucc) =>
            self ! PoisonPill

        case msg: FetchNotifications =>
            sender ! FetchNotificationsResponse(msg, Right(notifications.filter(n => !n.hasRead && !n.isWeekly)))

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
            val notification = n.copy(id = UUID(), echoedUserId = echoedUser.id)
            notifications = notifications.push(notification)
            ep(NotificationCreated(notification))
            mp(ScheduleOnce(
                    if (n.isWeekly) Week else Hour,
                    EmailNotifications(EchoedUserClientCredentials(echoedUser.id), n.notificationType),
                    Option(echoedUser.id + n.notificationType.map("-" + _).getOrElse(""))))


        case EmailNotifications(_, nt) if (echoedUser.hasEmail && echoedUserSettings.receiveNotificationEmail) =>
            val toEmail = MList[Notification]()
            notifications = notifications.map { n =>
                if (n.notificationType == nt && n.canEmail) {
                    val emailed = n.markAsEmailed
                    toEmail += emailed
                    ep(NotificationUpdated(emailed))
                    emailed
                } else n
            }

            val model = Map("notifications" -> toEmail.toList, "name" -> echoedUser.name)

            if (!toEmail.isEmpty) (nt: @unchecked) match {
                case None =>
                    mp(SendEmail(
                        echoedUser.email,
                        "%s, you have new notifications on Echoed!" format echoedUser.name,
                        "email_notifications",
                        model))
                case Some("weekly") =>
                    mp(SendEmail(
                        echoedUser.email,
                        "%s, New Stories for you this week on Echoed!" format echoedUser.name,
                        "email_weekly_notifications",
                        model))
            }


        case msg: RequestUsersFollowed =>
            val f = new Feed(userContext, followingUsers, null)
            sender ! RequestUsersFollowedResponse(msg, Right(f))

        case msg: RequestFollowers =>
            val f = new Feed(userContext, followedByUsers, null)
            sender ! RequestFollowersResponse(msg, Right(f))

        case msg: RequestPartnersFollowed =>
            val f = new Feed(userContext, followingPartners, null)
            sender ! RequestPartnersFollowedResponse(msg, Right(f))

        case msg @ FollowPartner(_, partnerId) =>
            val channel = sender
            mp(AddPartnerFollower(PartnerClientCredentials(partnerId), echoedUser)).onSuccess{
                case AddPartnerFollowerResponse(_, Right(partner)) => self.tell((msg, partner), channel)
            }

        case (msg @ FollowPartner(_, partnerId), partner: Partner) =>
            if (!followingPartners.exists(_.partnerId == partner.id)) followingPartners = PartnerFollower(partner.id, partner.name, partner.handle) :: followingPartners
            sender ! FollowPartnerResponse(msg, Right(followingPartners))
            ep(PartnerFollowerCreated(echoedUser.id, followingPartners.head))

        case msg @ UnFollowPartner(_, followingPartnerId) =>
            val channel = sender
            val (p, fps) = followingPartners.partition(_.partnerId == followingPartnerId)
            followingPartners = fps
            p.headOption.map {
                partner =>
                    mp(RemovePartnerFollower(PartnerClientCredentials(partner.partnerId), echoedUser))
            }
            channel ! UnFollowPartnerResponse(msg, Right(followingPartners))

        case AddPartnerFollowerResponse(_, Right(partner)) if (!followingPartners.exists(_.partnerId == partner.id)) =>
            followingPartners = PartnerFollower(partner.id, partner.name, partner.handle) :: followingPartners
            ep(PartnerFollowerCreated(echoedUser.id, followingPartners.head))

        case msg @ FollowUser(eucc, followerId) if (eucc.id != followerId) =>
            val channel = sender
            mp(AddFollower(EchoedUserClientCredentials(followerId), echoedUser)).onSuccess{
                case m @ AddFollowerResponse(_, Right(eu)) => self.tell((msg, eu), channel)
            }

        case (msg @ FollowUser(_, followerId), eu: EchoedUser) =>
            if (!followingUsers.exists(_.echoedUserId == eu.id)) followingUsers = Follower(eu) :: followingUsers
            sender ! FollowUserResponse(msg, Right(followingUsers))
            ep(FollowerCreated(echoedUser.id, followingUsers.head))


        case AddFollowerResponse(_, Right(eu)) if (!followingUsers.exists(_.echoedUserId == eu.id)) =>
            followingUsers = Follower(eu) :: followingUsers
            ep(FollowerCreated(echoedUser.id, followingUsers.head))

        case msg @ AddFollower(eucc, eu) if (!followedByUsers.exists(_.echoedUserId == eu.id)) =>
            followedByUsers = Follower(eu) :: followedByUsers
            mp(RegisterNotification(eucc, new Notification(
                eu,
                "follower",
                Map(
                    "subject" -> eu.name,
                    "action" -> "is following",
                    "object" -> "you",
                    "followerId" -> eu.id))))
            sender ! AddFollowerResponse(msg, Right(echoedUser))



        case msg @ UnFollowUser(_, followingUserId) =>

            val (fu, fus) = followingUsers.partition(_.echoedUserId == followingUserId)
            followingUsers = fus
            fu.headOption.map { f =>
                mp(RemoveFollower(EchoedUserClientCredentials(f.echoedUserId), echoedUser))
                ep(FollowerDeleted(echoedUser.id, f))
            }
            sender ! UnFollowUserResponse(msg, Right(followingUsers))

        case msg @ RemoveFollower(_, eu) =>
            followedByUsers = followedByUsers.filterNot(_.echoedUserId == eu.id)
            sender ! RemoveFollowerResponse(msg, Right(eu))

        case NotifyFollowers(_, n) =>
            val notification = n.copy(origin = echoedUser)
            followedByUsers.map(f => mp.tell(RegisterNotification(EUCC(f.echoedUserId), notification), self))

        case NotifyStoryUpdate(_, s) if (s.isOwnedBy(echoedUser.id)) =>
            if (s.isSelfModerated) privateContentManager.deleteContent(s)
            else privateContentManager.updateContent(s)

            if (s.isPublished) {
                val sp = s.published
                if (s.isModerated) publicContentManager.deleteContent(sp)
                else publicContentManager.updateContent(sp)
                mp.tell(EchoedUserMessageGroup(followedByUsers.map(f => NotifyStoryUpdate(EUCC(f.echoedUserId), sp))), self)
                mp.tell(PNSU(PartnerClientCredentials(sp.partner.id), sp), self)
            }

        case NotifyStoryUpdate(_, s) if (!s.isOwnedBy(echoedUser.id)) =>
            if (s.isModerated) followingContentManager.deleteContent(s)
            else followingContentManager.updateContent(s)

        case RegisterStory(story) =>
            activeStories.put(StoryId(story.id), sender)
            Option(story.echoId).map(e => activeStories.put(EchoId(e), sender))

        case msg @ InitStory(_, Some(storyId), _, _, _) => forwardToStory(msg, StoryId(storyId))
        case msg @ InitStory(_, _, Some(echoId), _, _) => forwardToStory(msg, EchoId(echoId))
        case msg @ InitStory(_, _, _, partnerId, _) => createStoryService(msg).forward(msg)


        case msg @ VoteStory(eucc, storyOwnerId, storyId, value) =>
            val channel = sender
            mp(new NewVote(new EchoedUserClientCredentials(storyOwnerId), echoedUser, storyId, value)).onSuccess {
                case NewVoteResponse(_, Right(votes)) =>
                    channel ! VoteStoryResponse(msg, Right(votes))
            }
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

        case msg: StoryIdentifiable with EchoedUserIdentifiable with Message =>
            forwardToStory(msg, StoryId(msg.storyId))
    }


    private def forwardToStory(msg: Message, identifiable: Identifiable) {
        activeStories.get(identifiable).headOption.cata(
            _.forward(msg),
            {
                val storyService = createStoryService(msg)
                storyService.forward(msg)
                activeStories.put(identifiable, storyService)
            })
    }

    private def createStoryService(msg: Message) =
            context.watch(storyServiceCreator(context, msg, echoedUser.copy(password = null, salt = null)))
}

private case class StoryId(id: String) extends Identifiable
private case class EchoId(id: String) extends Identifiable
