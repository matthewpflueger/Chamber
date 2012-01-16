package com.echoed.chamber.services.facebook

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.services.ActorClient


class FacebookServiceCreatorActorClient extends FacebookServiceCreator with ActorClient {

    @BeanProperty var facebookServiceCreatorActor: ActorRef = _

    def createFacebookServiceUsingCode(code: String, queryString: String) =
            (facebookServiceCreatorActor ? ("code", code, queryString)).mapTo[FacebookService]

    def createFacebookServiceUsingFacebookUserId(facebookUserId: String) =
            (facebookServiceCreatorActor ? ("facebookUserId", facebookUserId)).mapTo[FacebookService]

    def actorRef = facebookServiceCreatorActor
}
