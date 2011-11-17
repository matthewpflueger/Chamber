package com.echoed.chamber.services.facebook

import akka.actor.ActorRef
import reflect.BeanProperty


class FacebookServiceCreatorActorClient extends FacebookServiceCreator {

    @BeanProperty var facebookServiceCreatorActor: ActorRef = _

    def createFacebookServiceUsingCode(code: String) =
            (facebookServiceCreatorActor ? ("code", code)).mapTo[FacebookService]

    def createFacebookServiceUsingFacebookUserId(facebookUserId: String) =
            (facebookServiceCreatorActor ? ("facebookUserId", facebookUserId)).mapTo[FacebookService]
}
