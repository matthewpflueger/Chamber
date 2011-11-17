package com.echoed.chamber.services.echoeduser

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService

class EchoedUserServiceCreatorActorClient extends EchoedUserServiceCreator {

    @BeanProperty var echoedUserServiceCreatorActor: ActorRef = _

    def createEchoedUserServiceUsingId(id: String) =
            (echoedUserServiceCreatorActor ? ("id", id)).mapTo[EchoedUserService]

    def createEchoedUserServiceUsingFacebookService(facebookService: FacebookService) =
            (echoedUserServiceCreatorActor ? ("facebookService", facebookService)).mapTo[EchoedUserService]

    def createEchoedUserServiceUsingTwitterService(twitterService: TwitterService) =
            (echoedUserServiceCreatorActor ? ("twitterService",twitterService)).mapTo[EchoedUserService]

}
