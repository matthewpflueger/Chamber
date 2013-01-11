package com.echoed.chamber.services.echoeduser

import scalaz._
import Scalaz._
import akka.actor._
import akka.pattern._
import com.echoed.chamber.services._
import akka.util.Timeout
import com.google.common.collect.HashMultimap
import scala.collection.JavaConversions._
import scala.Right
import com.echoed.chamber.services.twitter.FetchUserForAuthToken
import com.echoed.chamber.services.twitter.FetchUserForAuthTokenResponse
import com.echoed.chamber.services.facebook.FetchMe
import akka.actor.Terminated
import com.echoed.chamber.services.facebook.FetchMeResponse
import com.echoed.util.Encrypter
import akka.actor.SupervisorStrategy.Stop


class EchoedUserServiceManager(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        facebookAccessCreator: (ActorContext) => ActorRef,
        twitterAccessCreator: (ActorContext) => ActorRef,
        echoedUserServiceCreator: (ActorContext, Message) => ActorRef,
        encrypter: Encrypter,
        implicit val timeout: Timeout = Timeout(20000)) extends EchoedService with ContentService{

    import context.dispatcher

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 0) {
        case _: Throwable ⇒ Stop
    }

    private val facebookAccess = facebookAccessCreator(context)
    private val twitterAccess = twitterAccessCreator(context)


    private val active = HashMultimap.create[String, ActorRef]()


    private def forwardForCredentials(msg: EchoedUserMessage, credentials: EchoedUserClientCredentials) {
        active.get(credentials.id).headOption.cata(
            _.forward(msg),
            {
                msg match {
                    case _: OnlineOnlyMessage =>
                        //Ignore Messages that should be online only
                    case _ =>
                        val echoedUserService = context.watch(echoedUserServiceCreator(
                                context,
                                LoginWithCredentials(credentials, msg, Some(context.sender))))
                        echoedUserService.forward(msg)
                        active.put(credentials.id, echoedUserService)
                }
            })
    }


    def handle = {
        case Terminated(ref) => active.values.removeAll(active.values.filter(_ == ref))


        case RegisterEchoedUserService(echoedUser) =>
            active.put(echoedUser.id, context.sender)
            Option(echoedUser.email).foreach(id => active.put(id, context.sender))
            Option(echoedUser.screenName).foreach(id => active.put(id, context.sender))
            Option(echoedUser.facebookId).foreach(id => active.put(id, context.sender))
            Option(echoedUser.twitterId).foreach(id => active.put(id, context.sender))


        case msg @ Logout(eucc) => active.get(eucc.id).headOption.foreach(_.forward(msg))


        case msg @ LoginWithFacebookUser(facebookUser, correlation, channel) =>
            active
                    .get(facebookUser.facebookId).headOption
                    .cata(
                _ ! msg,
                {
                    val echoedUserService = context.watch(echoedUserServiceCreator(context, msg))
                    active.put(facebookUser.facebookId, echoedUserService)
                })


        case msg @ LoginWithFacebook(fb) =>
            val me = context.self
            val channel = Option(context.sender)

            (facebookAccess ? FetchMe(fb)).onSuccess {
                case FetchMeResponse(_, Right(facebookUser)) => me ! LoginWithFacebookUser(facebookUser, msg, channel)
            }


        case msg @ LoginWithTwitterUser(twitterUser, correlation, channel) =>
            active
                    .get(twitterUser.twitterId).headOption
                    .cata(
                _ ! msg,
                {
                    val echoedUserService = context.watch(echoedUserServiceCreator(context, msg))
                    active.put(twitterUser.twitterId, echoedUserService)
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


        case msg: EchoedUserIdentifiable with EchoedUserMessage => forwardForCredentials(msg, msg.credentials)
    }
}
