package com.echoed.chamber.services.echoeduser

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService

class EchoedUserServiceCreatorActorClient extends EchoedUserServiceCreator {

    @BeanProperty var echoedUserServiceCreatorActor: ActorRef = _

    def createEchoedUserServiceUsingId(id: String) =
            (echoedUserServiceCreatorActor ? (CreateEchoedUserServiceWithId(id))).mapTo[CreateEchoedUserServiceWithIdResponse]

    def createEchoedUserServiceUsingFacebookService(facebookService: FacebookService) =
            (echoedUserServiceCreatorActor ? (CreateEchoedUserServiceWithFacebookService(facebookService))).mapTo[CreateEchoedUserServiceWithFacebookServiceResponse]

    def createEchoedUserServiceUsingTwitterService(twitterService: TwitterService) =
            (echoedUserServiceCreatorActor ? (CreateEchoedUserServiceWithTwitterService(twitterService))).mapTo[CreateEchoedUserServiceWithTwitterServiceResponse]

}
