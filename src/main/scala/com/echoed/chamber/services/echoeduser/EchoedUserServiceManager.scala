package com.echoed.chamber.services.echoeduser

import scalaz._
import Scalaz._
import akka.actor._
import akka.pattern._
import com.echoed.chamber.services._
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
import com.echoed.util.{ScalaObjectMapper, Encrypter}


class EchoedUserServiceManager(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        facebookAccessCreator: (ActorContext) => ActorRef,
        twitterAccessCreator: (ActorContext) => ActorRef,
        echoedUserServiceCreator: (ActorContext, Message) => ActorRef,
        encrypter: Encrypter,
        implicit val timeout: Timeout = Timeout(20000)) extends EchoedService {

    private val facebookAccess = facebookAccessCreator(context)
    private val twitterAccess = twitterAccessCreator(context)


    private val active = HashMultimap.create[Identifiable, ActorRef]()


    private def forwardForCredentials(msg: EchoedUserMessage, credentials: EchoedUserClientCredentials) {
        active.get(EchoedUserId(credentials.echoedUserId)).headOption.cata(
            _.forward(msg),
            {
                val echoedUserService = context.watch(echoedUserServiceCreator(context, LoginWithCredentials(credentials)))
                echoedUserService.forward(msg)
                active.put(EchoedUserId(credentials.echoedUserId), echoedUserService)
            })
    }


    def handle = {
        case Terminated(ref) => active.values.removeAll(active.values.filter(_ == ref))


        case RegisterEchoedUserService(echoedUser) =>
            active.put(EchoedUserId(echoedUser.id), context.sender)
            Option(echoedUser.email).foreach(id => active.put(Email(id), context.sender))
            Option(echoedUser.screenName).foreach(id => active.put(ScreenName(id), context.sender))
            Option(echoedUser.facebookId).foreach(id => active.put(FacebookId(id), context.sender))
            Option(echoedUser.twitterId).foreach(id => active.put(TwitterId(id), context.sender))


        case msg @ Logout(eucc) => active.get(EchoedUserId(eucc.echoedUserId)).headOption.foreach(_.forward(msg))


        case msg @ LoginWithCode(code) =>
            val channel = context.sender
            val map = ScalaObjectMapper(encrypter.decrypt(code), classOf[Map[String, String]])
            mp(LoginWithEmailPassword(map("email"), map("password"))).onSuccess {
                case LoginWithEmailPasswordResponse(_, Left(e)) =>
                    channel ! LoginWithCodeResponse(msg, Left(e))
                case LoginWithEmailPasswordResponse(_, Right(echoedUser)) =>
                    channel ! LoginWithCodeResponse(msg, Right(echoedUser))
            }


        case msg @ LoginWithFacebookUser(facebookUser, correlation, channel) =>
            active
                    .get(FacebookId(facebookUser.facebookId)).headOption
                    .cata(
                _ ! msg,
                {
                    val echoedUserService = context.watch(echoedUserServiceCreator(context, msg))
                    active.put(FacebookId(facebookUser.facebookId), echoedUserService)
                })


        case msg @ LoginWithFacebook(fb) =>
            val me = context.self
            val channel = Option(context.sender)

            (facebookAccess ? FetchMe(fb)).onSuccess {
                case FetchMeResponse(_, Right(facebookUser)) => me ! LoginWithFacebookUser(facebookUser, msg, channel)
            }


        case msg @ LoginWithTwitterUser(twitterUser, correlation, channel) =>
            active
                    .get(TwitterId(twitterUser.twitterId)).headOption
                    .cata(
                _ ! msg,
                {
                    val echoedUserService = context.watch(echoedUserServiceCreator(context, msg))
                    active.put(TwitterId(twitterUser.twitterId), echoedUserService)
                })


        case msg: GetTwitterAuthenticationUrl => twitterAccess.forward(msg)


        case msg @ LoginWithTwitter(authToken, authVerifier) =>
            val me = context.self
            val channel = Option(context.sender)

            (twitterAccess ? FetchUserForAuthToken(authToken, authVerifier)).onSuccess {
                case FetchUserForAuthTokenResponse(_, Right(twitterUser)) =>
                        me ! LoginWithTwitterUser(twitterUser, msg, channel)
            }


        case msg @ RegisterLogin(_, email, screenName, _, credentials) =>
            credentials.cata(
                forwardForCredentials(msg, _),
                context.watch(echoedUserServiceCreator(context, msg)).forward(msg))


        case msg: EmailOrScreenNameIdentifiable with EchoedUserMessage =>
            active
                    .get(Email(msg.emailOrScreenName))
                    .headOption
                    .orElse(active.get(ScreenName(msg.emailOrScreenName)).headOption)
                    .cata(
                _.forward(msg),
                {
                    val echoedUserService = context.watch(echoedUserServiceCreator(
                            context,
                            LoginWithEmailOrScreenName(msg.emailOrScreenName, msg, Option(sender))))
                    echoedUserService.forward(msg)
                    active.put(Email(msg.emailOrScreenName), echoedUserService)
                })


        case msg: EchoedUserIdentifiable with EchoedUserMessage => forwardForCredentials(msg, msg.credentials)
    }
}


case class EchoedUserId(id: String) extends Identifiable
case class Email(id: String) extends Identifiable
case class ScreenName(id: String) extends Identifiable
case class FacebookId(id: String) extends Identifiable
case class TwitterId(id: String) extends Identifiable


