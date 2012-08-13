package com.echoed.chamber.services.echoeduser

import scalaz._
import Scalaz._
import akka.actor._
import com.echoed.cache.CacheManager
import akka.pattern._
import com.echoed.chamber.dao.views.{FeedDao, ClosetDao}
import com.echoed.chamber.dao._
import com.echoed.chamber.dao.partner.{PartnerDao, PartnerSettingsDao}
import com.echoed.chamber.services._
import org.springframework.transaction.support.TransactionTemplate
import akka.util.Timeout
import com.echoed.chamber.domain.Identifiable
import com.google.common.collect.HashMultimap
import scala.collection.JavaConversions._
import scala.Left
import scala.Right
import com.echoed.chamber.services.twitter.FetchUserForAuthToken
import com.echoed.chamber.services.twitter.FetchUserForAuthTokenResponse
import com.echoed.chamber.services.facebook.FetchMe
import akka.actor.Terminated
import com.echoed.chamber.services.facebook.FetchMeResponse


class EchoedUserServiceManager(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        facebookAccessCreator: (ActorContext) => ActorRef,
        twitterAccessCreator: (ActorContext) => ActorRef,
        echoedUserDao: EchoedUserDao,
        closetDao: ClosetDao,
        feedDao: FeedDao,
        partnerSettingsDao: PartnerSettingsDao,
        echoDao: EchoDao,
        echoedFriendDao: EchoedFriendDao,
        echoMetricsDao: EchoMetricsDao,
        partnerDao: PartnerDao,
        storyDao: StoryDao,
        chapterDao: ChapterDao,
        chapterImageDao: ChapterImageDao,
        imageDao: ImageDao,
        commentDao: CommentDao,
        facebookFriendDao: FacebookFriendDao,
        twitterFollowerDao: TwitterFollowerDao,
        facebookPostDao: FacebookPostDao,
        twitterStatusDao: TwitterStatusDao,
        transactionTemplate: TransactionTemplate,
        cacheManager: CacheManager,
        storyGraphUrl: String,
        echoClickUrl: String,
        implicit val timeout: Timeout = Timeout(20000)) extends EchoedService {

    private val facebookAccess = facebookAccessCreator(context)
    private val twitterAccess = twitterAccessCreator(context)


    private val active = HashMultimap.create[Identifiable, ActorRef]()


    def handle = {
        case Terminated(ref) => active.values.removeAll(active.values.filter(_ == ref))


        case RegisterEchoedUserService(echoedUser) =>
            active.put(EchoedUserId(echoedUser.id), context.sender)
            Option(echoedUser.email).foreach(email => active.put(Email(email), context.sender))
            Option(echoedUser.facebookId).foreach(id => active.put(FacebookId(id), context.sender))
            Option(echoedUser.twitterId).foreach(id => active.put(TwitterId(id), context.sender))


        case msg @ Logout(eucc) => active.get(EchoedUserId(eucc.echoedUserId)).headOption.foreach(_.forward(msg))


        case msg @ LoginWithFacebookUser(facebookUser, correlation, channel) =>
            active
                    .get(FacebookId(facebookUser.facebookId)).headOption
                    .orElse(active.get(Email(facebookUser.email)).headOption)
                    .cata(
                _ ! msg,
                {
                    val echoedUserService = context.watch(context.actorOf(Props(new EchoedUserService(
                            mp,
                            ep,
                            initMessage = msg,
                            echoedUserDao = echoedUserDao,
                            closetDao = closetDao,
                            echoedFriendDao = echoedFriendDao,
                            feedDao = feedDao,
                            partnerSettingsDao = partnerSettingsDao,
                            echoDao = echoDao,
                            partnerDao = partnerDao,
                            echoMetricsDao = echoMetricsDao,
                            storyDao = storyDao,
                            chapterDao = chapterDao,
                            chapterImageDao = chapterImageDao,
                            commentDao = commentDao,
                            imageDao = imageDao,
                            transactionTemplate = transactionTemplate,
                            storyGraphUrl = storyGraphUrl,
                            facebookFriendDao = facebookFriendDao,
                            twitterFollowerDao = twitterFollowerDao,
                            facebookPostDao = facebookPostDao,
                            twitterStatusDao = twitterStatusDao,
                            echoClickUrl = echoClickUrl))))

                    active.put(FacebookId(facebookUser.facebookId), echoedUserService)
                    active.put(Email(facebookUser.email), echoedUserService)
                })


        case msg @ LoginWithFacebook(Left(fc)) =>
            val me = context.self
            val channel = Option(context.sender)

            (facebookAccess ? FetchMe(Left(fc))).onSuccess {
                case FetchMeResponse(_, Right(facebookUser)) => me ! LoginWithFacebookUser(facebookUser, msg, channel)
            }


        case msg @ LoginWithFacebook(Right(fat)) =>
            val me = context.self
            val channel = Option(context.sender)

            (facebookAccess ? FetchMe(Right(fat))).onSuccess {
                case FetchMeResponse(_, Right(facebookUser)) => me ! LoginWithFacebookUser(facebookUser, msg, channel)
            }


        case msg @ LoginWithTwitterUser(twitterUser, correlation, channel) =>
            active
                    .get(TwitterId(twitterUser.twitterId)).headOption
                    .cata(
                _ ! msg,
                {
                    val echoedUserService = context.watch(context.actorOf(Props(new EchoedUserService(
                            mp,
                            ep,
                            initMessage = msg,
                            echoedUserDao = echoedUserDao,
                            closetDao = closetDao,
                            echoedFriendDao = echoedFriendDao,
                            feedDao = feedDao,
                            partnerSettingsDao = partnerSettingsDao,
                            echoDao = echoDao,
                            partnerDao = partnerDao,
                            echoMetricsDao = echoMetricsDao,
                            storyDao = storyDao,
                            chapterDao = chapterDao,
                            chapterImageDao = chapterImageDao,
                            commentDao = commentDao,
                            imageDao = imageDao,
                            facebookFriendDao = facebookFriendDao,
                            twitterFollowerDao = twitterFollowerDao,
                            transactionTemplate = transactionTemplate,
                            storyGraphUrl = storyGraphUrl,
                            facebookPostDao = facebookPostDao,
                            twitterStatusDao = twitterStatusDao,
                            echoClickUrl = echoClickUrl))))

                    active.put(TwitterId(twitterUser.twitterId), echoedUserService)
                })


        case msg: GetTwitterAuthenticationUrl => twitterAccess.forward(msg)


        case msg @ LoginWithTwitter(authToken, authVerifier) =>
            val me = context.self
            val channel = Option(context.sender)

            (twitterAccess ? FetchUserForAuthToken(authToken, authVerifier)).onSuccess {
                case FetchUserForAuthTokenResponse(_, Right(twitterUser)) => me ! LoginWithTwitterUser(twitterUser, msg, channel)
            }


        case msg: EchoedUserIdentifiable =>
            active.get(EchoedUserId(msg.echoedUserId)).headOption.cata(
                ref => {
                    log.debug("Forwarding {} to {}", msg, ref.path)
                    ref.forward(msg)
                },
                {
                    val echoedUserService = context.watch(context.actorOf(Props(new EchoedUserService(
                            mp,
                            ep,
                            initMessage = LoginWithCredentials(msg.credentials),
                            echoedUserDao = echoedUserDao,
                            closetDao = closetDao,
                            echoedFriendDao = echoedFriendDao,
                            feedDao = feedDao,
                            partnerSettingsDao = partnerSettingsDao,
                            echoDao = echoDao,
                            partnerDao = partnerDao,
                            echoMetricsDao = echoMetricsDao,
                            storyDao = storyDao,
                            chapterDao = chapterDao,
                            chapterImageDao = chapterImageDao,
                            commentDao = commentDao,
                            imageDao = imageDao,
                            facebookFriendDao = facebookFriendDao,
                            twitterFollowerDao = twitterFollowerDao,
                            transactionTemplate = transactionTemplate,
                            storyGraphUrl = storyGraphUrl,
                            facebookPostDao = facebookPostDao,
                            twitterStatusDao = twitterStatusDao,
                            echoClickUrl = echoClickUrl))))

                    echoedUserService.forward(msg)
                    active.put(EchoedUserId(msg.echoedUserId), echoedUserService)
                })
    }

}

case class EchoedUserId(id: String) extends Identifiable
case class Email(id: String) extends Identifiable
case class FacebookId(id: String) extends Identifiable
case class TwitterId(id: String) extends Identifiable


