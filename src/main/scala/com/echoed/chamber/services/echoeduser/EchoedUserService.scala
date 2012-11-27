package com.echoed.chamber.services.echoeduser

import scalaz._
import Scalaz._
import scala.collection.JavaConversions._
import scala.collection.mutable.{ListBuffer => MList}
import com.echoed.chamber.services._
import akka.actor._
import akka.pattern._
import akka.util.duration._
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
import com.echoed.chamber.services.state.FacebookUserNotFound
import akka.actor.Terminated
import com.echoed.chamber.services.facebook.FacebookAccessToken
import com.echoed.chamber.services.ScatterResponse
import com.echoed.chamber.services.state.ReadForCredentials
import scala.Left
import com.echoed.chamber.services.twitter.FetchFollowers
import com.echoed.chamber.services.facebook.FetchFriends
import com.echoed.chamber.services.state.TwitterUserNotFound
import akka.actor.OneForOneStrategy
import com.echoed.chamber.services.facebook.PublishAction
import com.echoed.chamber.domain.views.{ClosetPersonal, Closet, EchoFull, EchoedUserStoryFeed}
import com.echoed.chamber.services.email.SendEmail
import com.echoed.chamber.services.state.ReadForCredentialsResponse
import scala.Right
import com.echoed.chamber.services.facebook.FetchFriendsResponse
import com.echoed.chamber.services.Scatter
import com.echoed.chamber.services.state.ReadForTwitterUser
import com.echoed.chamber.services.state.ReadForTwitterUserResponse
import com.echoed.chamber.services.state.ReadForFacebookUserResponse
import com.echoed.chamber.services.feed.{GetUserPrivateStoryFeedResponse, GetUserPrivateStoryFeed, GetUserPublicStoryFeed, GetUserPublicStoryFeedResponse}
import com.echoed.chamber.domain.public.EchoedUserPublic
import com.echoed.chamber.services.echoeduser.{EchoedUserClientCredentials => EUCC}
import com.echoed.chamber.services.partner.{AddPartnerFollowerResponse, AddPartnerFollower, PartnerClientCredentials}


class EchoedUserService(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        initMessage: Message,
        storyServiceCreator: (ActorContext, Message, EchoedUser) => ActorRef,
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
    private var followingPartners = List[PartnerFollower]()


    private val activeStories = HashMultimap.create[Identifiable, ActorRef]()

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 0, withinTimeRange = 1 minute) {
        case _: Throwable ⇒ Stop
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
        followingPartners = euss.followingPartners
    }

    override def preStart() {
        super.preStart()
        initMessage match {
            case LoginWithCredentials(credentials, _, _) => mp.tell(ReadForCredentials(credentials), self)
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


        case msg: GetExhibit =>
            mp(GetUserPrivateStoryFeed(echoedUser.id, msg.page))
                    .mapTo[GetUserPrivateStoryFeedResponse]
                    .map(_.resultOrException)
                    .map { r =>
                        val closet = new Closet(echoedUser.id, echoedUser).copy(stories = r.stories)
                        GetExhibitResponse(msg, Right(new ClosetPersonal(closet)))
                    }.pipeTo(sender)


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
            notifications = notifications.push(n)
            ep(NotificationCreated(n))
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

            if (!toEmail.isEmpty) nt match {
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


        case msg: ListFollowingUsers => sender ! ListFollowingUsersResponse(msg, Right(followingUsers))
        case msg: ListFollowedByUsers => sender ! ListFollowedByUsersResponse(msg, Right(followedByUsers))
        case msg: ListFollowingPartners => sender ! ListFollowingPartnersResponse(msg, Right(followingPartners))

        case msg @ FollowPartner(_, partnerId) =>
            sender ! FollowPartnerResponse(msg, Right(true))
            mp.tell(AddPartnerFollower(PartnerClientCredentials(partnerId), echoedUser), self)

        case AddPartnerFollowerResponse(_, Right(partner)) if (!followingPartners.exists(_.partnerId == partner.id)) =>
            followingPartners = PartnerFollower(partner.id, partner.name, partner.handle) :: followingPartners
            ep(PartnerFollowerCreated(echoedUser.id, followingPartners.head))

        case msg @ FollowUser(eucc, followerId) if (eucc.id != followerId) =>
            sender ! FollowUserResponse(msg, Right(true))
            mp.tell(AddFollower(EchoedUserClientCredentials(followerId), echoedUser), self)

        case AddFollowerResponse(_, Right(eu)) if (!followingUsers.exists(_.echoedUserId == eu.id)) =>
            followingUsers = Follower(eu) :: followingUsers
            ep(FollowerCreated(echoedUser.id, followingUsers.head))

        case msg @ AddFollower(eucc, eu) if (!followedByUsers.exists(_.echoedUserId == eu.id)) =>
            sender ! AddFollowerResponse(msg, Right(echoedUser))
            followedByUsers = Follower(eu) :: followedByUsers
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
            sender ! UnFollowUserResponse(msg, Right(true))
            val (fu, fus) = followingUsers.partition(_.echoedUserId == followingUserId)
            followingUsers = fus
            fu.headOption.map { f =>
                mp(RemoveFollower(EchoedUserClientCredentials(f.echoedUserId), echoedUser))
                ep(FollowerDeleted(echoedUser.id, f))
            }

        case msg @ RemoveFollower(_, eu) => followedByUsers = followedByUsers.filterNot(_.echoedUserId == eu.id)

        case NotifyFollowers(_, notification) =>
            followedByUsers.foreach { f =>
                mp(RegisterNotification(
                    EchoedUserClientCredentials(f.echoedUserId),
                    new Notification(
                            f.echoedUserId,
                            echoedUser,
                            notification.category,
                            notification.value)))
            }

        case RegisterStory(story) =>
            activeStories.put(StoryId(story.id), sender)
            Option(story.echoId).map(e => activeStories.put(EchoId(e), sender))

        case msg @ InitStory(_, Some(storyId), _, _) => forwardToStory(msg, StoryId(storyId))
        case msg @ InitStory(_, _, Some(echoId), _) => forwardToStory(msg, EchoId(echoId))
        case msg @ InitStory(_, _, _, partnerId) => createStoryService(msg).forward(msg)

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
            mp(GetUserPublicStoryFeed(echoedUser.id, page)).onSuccess {
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
