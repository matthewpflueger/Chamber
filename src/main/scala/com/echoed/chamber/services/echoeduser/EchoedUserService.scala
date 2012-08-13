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
import com.echoed.chamber.services.echoeduser.{EchoToFacebookResponse => ETFR, EchoToFacebook => ETF}
import akka.util.duration._
import akka.actor.SupervisorStrategy.Stop
import com.echoed.chamber.dao.partner.{PartnerDao, PartnerSettingsDao}
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.util.TransactionUtils._
import org.springframework.transaction.TransactionStatus
import java.util.Date
import com.echoed.util.ScalaObjectMapper
import akka.util.Timeout
import com.echoed.chamber.services.twitter._
import com.echoed.chamber.domain.ChapterInfo
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.services.twitter.FetchFollowersResponse
import com.echoed.chamber.services.feed.GetUserPublicStoryFeedResponse
import com.echoed.chamber.domain.views.EchoViewDetail
import com.echoed.chamber.domain.TwitterUser
import scala.Some
import com.echoed.chamber.domain.views.Feed
import com.echoed.chamber.domain.views.FriendCloset
import com.echoed.chamber.domain.TwitterStatus
import com.echoed.chamber.services.state.ReadForFacebookUser
import com.echoed.chamber.domain.EchoedFriend
import com.echoed.chamber.services.twitter.GetFollowersResponse
import com.echoed.chamber.domain.FacebookUser
import com.echoed.chamber.domain.ChapterImage
import com.echoed.chamber.services.state.FacebookUserNotFound
import com.echoed.chamber.services.facebook.FacebookAccessToken
import com.echoed.chamber.services.ScatterResponse
import com.echoed.chamber.services.state.ReadForCredentials
import scala.Left
import com.echoed.chamber.services.twitter.FetchFollowers
import com.echoed.chamber.domain.Story
import com.echoed.chamber.domain.partner.StoryPrompts
import com.echoed.chamber.domain.views.EchoView
import com.echoed.chamber.domain.views.FriendFeed
import com.echoed.chamber.services.feed.GetUserPublicStoryFeed
import com.echoed.chamber.services.facebook.FetchFriends
import com.echoed.chamber.services.tag.TagReplaced
import com.echoed.chamber.services.state.TwitterUserNotFound
import akka.actor.OneForOneStrategy
import com.echoed.chamber.domain.views.StoryFull
import com.echoed.chamber.domain.FacebookPost
import com.echoed.chamber.services.facebook.Post
import com.echoed.chamber.services.facebook.PostResponse
import com.echoed.chamber.services.facebook.PublishAction
import com.echoed.chamber.domain.views.EchoFull
import com.echoed.chamber.domain.views.ClosetPersonal
import com.echoed.chamber.domain.views.Closet
import com.echoed.chamber.domain.Chapter
import com.echoed.chamber.services.state.ReadForCredentialsResponse
import scala.Right
import com.echoed.chamber.services.facebook.FetchFriendsResponse
import com.echoed.chamber.services.state.EchoedUserServiceState
import com.echoed.chamber.services.Scatter
import com.echoed.chamber.services.state.ReadForTwitterUser
import com.echoed.chamber.services.state.ReadForTwitterUserResponse
import com.echoed.chamber.services.state.ReadForFacebookUserResponse
import com.echoed.chamber.domain.StoryInfo


class EchoedUserService(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        initMessage: Message,
        echoedUserDao: EchoedUserDao,
        closetDao: ClosetDao,
        echoedFriendDao: EchoedFriendDao,
        feedDao: FeedDao,
        partnerSettingsDao: PartnerSettingsDao,
        echoDao: EchoDao,
        echoMetricsDao: EchoMetricsDao,
        partnerDao: PartnerDao,
        storyDao: StoryDao,
        chapterDao: ChapterDao,
        chapterImageDao: ChapterImageDao,
        commentDao: CommentDao,
        imageDao: ImageDao,
        facebookFriendDao: FacebookFriendDao,
        twitterFollowerDao: TwitterFollowerDao,
        facebookPostDao: FacebookPostDao,
        twitterStatusDao: TwitterStatusDao,
        transactionTemplate: TransactionTemplate,
        storyGraphUrl: String,
        echoClickUrl: String,
        implicit val timeout: Timeout = Timeout(20000),
        lifespan: Int = 30) extends OnlineOfflineService {


    private var echoedUser: EchoedUser = _
    private var facebookUser: Option[FacebookUser] = None
    private var twitterUser: Option[TwitterUser] = None


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
                mp(FetchFriends(FacebookAccessToken(fu.accessToken, Some(fu.facebookId)), fu.id)).pipeTo(self)
                //hack to reset our posts to be crawled - really should send a message to FacebookPostCrawler to crawl our posts...
                facebookPostDao.resetPostsToCrawl(fu.id)
                fu
            }

        becomeOnline
        context.parent ! RegisterEchoedUserService(echoedUser)
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
                mp(FetchFollowers(
                    tu.accessToken,
                    tu.accessTokenSecret,
                    tu.id,
                    tu.twitterId.toLong)).pipeTo(self)
                tu
            }

        becomeOnline
        context.parent ! RegisterEchoedUserService(echoedUser)
        msg.correlationSender.foreach(_ ! LoginWithTwitterResponse(msg.correlation, Right(echoedUser)))
    }


    private def setState(euss: EchoedUserServiceState) {
        echoedUser = euss.echoedUser
        twitterUser = euss.twitterUser
        facebookUser = euss.facebookUser
    }

    override def preStart() {
        super.preStart()
        initMessage match {
            case LoginWithCredentials(credentials) => mp(ReadForCredentials(credentials)).pipeTo(self)
            case LoginWithFacebookUser(facebookUser, _, _) => mp(ReadForFacebookUser(facebookUser)).pipeTo(self)
            case LoginWithTwitterUser(twitterUser, _, _) => mp(ReadForTwitterUser(twitterUser)).pipeTo(self)
        }
        context.setReceiveTimeout(lifespan minutes)
    }


    def init = {
        case msg @ ReadForCredentialsResponse(_, Right(euss)) =>
            setState(euss)
            becomeOnline
            context.parent ! RegisterEchoedUserService(echoedUser)


        case msg @ ReadForFacebookUserResponse(_, Left(FacebookUserNotFound(fu, _))) =>
            echoedUser = new EchoedUser(fu)
            handleLoginWithFacebookUser(initMessage.asInstanceOf[LoginWithFacebookUser])
            ep(EchoedUserCreated(echoedUser, facebookUser, twitterUser))


        case msg @ ReadForFacebookUserResponse(_, Right(euss)) =>
            setState(euss)
            handleLoginWithFacebookUser(initMessage.asInstanceOf[LoginWithFacebookUser])
            ep(EchoedUserUpdated(echoedUser, facebookUser, twitterUser))


        case msg @ ReadForTwitterUserResponse(_, Left(TwitterUserNotFound(tu, _))) =>
            echoedUser = new EchoedUser(tu)
            handleLoginWithTwitterUser(initMessage.asInstanceOf[LoginWithTwitterUser])
            ep(EchoedUserCreated(echoedUser, facebookUser, twitterUser))


        case msg @ ReadForTwitterUserResponse(_, Right(euss)) =>
            setState(euss)
            handleLoginWithTwitterUser(initMessage.asInstanceOf[LoginWithTwitterUser])
            ep(EchoedUserUpdated(echoedUser, facebookUser, twitterUser))
    }


    def online = {
        case msg @ LoginWithFacebookUser(fu, correlation, correlationSender) =>
            handleLoginWithFacebookUser(msg)
            ep(EchoedUserUpdated(echoedUser, facebookUser, twitterUser))


        case msg @ LoginWithTwitterUser(tu, correlation, correlationSender) =>
            handleLoginWithTwitterUser(msg)
            ep(EchoedUserUpdated(echoedUser, facebookUser, twitterUser))


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


        case msg @ PublishFacebookAction(eucc, action, obj, objUrl, origin) =>
            log.error("TODO: Publish origin {}", origin)
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
                mp(GetUserPublicStoryFeed(echoedUser.id, msg.page, "echoed")).onSuccess({
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


        case msg @ CreateStory(_, title, imageId, partnerId, echoId, productInfo) =>
            val channel = context.sender

            val echo = echoId.map(echoDao.findByIdAndEchoedUserId(_, echoedUser.id))

            val partner = partnerDao.findByIdOrHandle(partnerId.getOrElse(echo.map(_.partnerId).getOrElse("Echoed")))
            val partnerSettings = partnerSettingsDao.findByIdOrPartnerHandle(echo.map(_.partnerSettingsId).getOrElse("Echoed"))
            val image = imageDao.findById(imageId)

            val story = new Story(echoedUser, partner, partnerSettings, image, title, echo, productInfo)
            storyDao.insert(story)
            ep.publish(StoryUpdated(story.id))
            channel ! CreateStoryResponse(msg, Right(story))


        case msg @ UpdateStory(_, storyId, title, imageId) =>
            val channel  = context.sender

            val story = storyDao
                    .findByIdAndEchoedUserId(storyId, echoedUser.id)
                    .copy(title = title, image = imageDao.findById(imageId))
            storyDao.update(story)
            channel ! UpdateStoryResponse(msg, Right(story))


        case msg @ TagStory(_, storyId, tagId) =>
            val channel = context.sender
            val story = storyDao
                        .findByIdAndEchoedUserId(storyId, echoedUser.id)
            val originalTag = story.tag
            val newStory = story.copy(tag = tagId)
            storyDao.update(newStory)
            ep.publish(TagReplaced(originalTag, tagId) )
            ep.publish(StoryUpdated(storyId))
            channel ! TagStoryResponse(msg, Right(newStory))


        case msg @ CreateChapter(eucc, storyId, title, text, imageIds) =>
            val channel = context.sender
            val me = self

            val story = storyDao.findByIdAndEchoedUserId(storyId, echoedUser.id)
            val chapter = new Chapter(story, title, text)
            val chapterImages = imageIds.cata(
                ids => ids.map(id => new ChapterImage(chapter, imageDao.findById(id))),
                Array[ChapterImage]())

            transactionTemplate.execute { status: TransactionStatus =>
                chapterDao.insert(chapter)
                chapterImages.foreach(chapterImageDao.insert(_))
                storyDao.update(story) //Update the story to get new timestamp
            }

            ep.publish(StoryUpdated(storyId))
            me ! PublishFacebookAction(eucc, "update", "story", storyGraphUrl + storyId, "Echoed")
            channel ! CreateChapterResponse(msg, Right(ChapterInfo(chapter, chapterImages)))


        case msg @ UpdateChapter(_, chapterId, title, text, imageIds) =>
            val channel = context.sender

            val chapter = chapterDao
                    .findByIdAndEchoedUserId(chapterId, echoedUser.id)
                    .copy(title = title, text = text)

            val chapterImages = imageIds.cata(
                ids => ids.map(id => new ChapterImage(chapter, imageDao.findById(id))),
                Array[ChapterImage]())
            ep.publish(StoryUpdated(chapter.storyId))
            transactionTemplate.execute { status: TransactionStatus =>
                chapterDao.update(chapter)
                chapterImageDao.deleteByChapterId(chapter.id)
                chapterImages.foreach(chapterImageDao.insert(_))
            }
            channel ! UpdateChapterResponse(msg, Right(ChapterInfo(chapter, chapterImages)))


        case msg @ CreateComment(eucc, storyOwnerId, storyId, chapterId, text, parentCommentId) =>
            mp(NewComment(
                    new EchoedUserClientCredentials(storyOwnerId),
                    echoedUser,
                    storyId,
                    chapterId,
                    text,
                    parentCommentId))
                .mapTo[NewCommentResponse]
                .map(ncr => CreateCommentResponse(msg, ncr.value))
                .pipeTo(context.sender)


        case msg @ NewComment(eucc, byEchoedUser, storyId, chapterId, text, parentCommentId) =>
            val channel = context.sender
            val me = self

            val comment = new com.echoed.chamber.domain.Comment(
                chapterDao.findByIdAndStoryId(chapterId, storyId),
                byEchoedUser,
                text,
                parentCommentId.map(commentDao.findByIdAndChapterId(_, chapterId)))
            commentDao.insert(comment)
            ep.publish(StoryUpdated(storyId))
            me ! PublishFacebookAction(eucc, "comment_on", "story", storyGraphUrl + storyId, "echoed")
            channel ! NewCommentResponse(msg, Right(comment))


        case msg @ InitStory(_, storyId, echoId, partnerId) =>
            val channel = context.sender
            val story = storyId.map(storyDao.findByIdAndEchoedUserId(_, echoedUser.id))

            val echo = story
                    .map(s => s.echoId)
                    .map(e => echoDao.findByIdAndEchoedUserId(e , echoedUser.id))
                    .orElse(echoId.map(echoDao.findByIdAndEchoedUserId(_, echoedUser.id)))

            val storyFull = story.map { s =>
                val chapters = chapterDao.findByStoryId(s.id)
                val chapterImages = chapterImageDao.findByStoryId(s.id)
                val comments = commentDao.findByStoryId(s.id)
                StoryFull(s.id, s, echoedUser, chapters, chapterImages, comments)
            }.orElse(echo.map(e => feedDao.findStoryByEchoId(e.id)))


            val partner = story
                    .map(s => partnerDao.findById(s.partnerId))
                    .orElse(echo.map(e => partnerDao.findById(e.partnerId)))
                    .orElse(partnerId.map(partnerDao.findByIdOrHandle(_)))
                    .orElse(Option(partnerDao.findByIdOrHandle("Echoed")))
                    .get

            val storyPrompts = new ScalaObjectMapper().readValue(
                    Option(partnerSettingsDao.findByActiveOn(partner.id, new Date()).storyPrompts)
                        .getOrElse(partnerSettingsDao.findByIdOrPartnerHandle("Echoed").storyPrompts),
                    classOf[StoryPrompts])

            channel ! InitStoryResponse(
                    msg,
                    Right(StoryInfo(echoedUser, echo.orNull, partner, storyPrompts, storyFull.orNull)))
    }

}
