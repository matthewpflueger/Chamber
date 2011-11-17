package com.echoed.chamber.services.facebook

import reflect.BeanProperty
import akka.actor.ActorRef


class FacebookServiceLocatorActorClient extends FacebookServiceLocator {

    @BeanProperty var facebookServiceLocatorActor: ActorRef = _

    def getFacebookServiceWithCode(code: String) =
            (facebookServiceLocatorActor ? ("code", code)).mapTo[FacebookService]


    def getFacebookServiceWithFacebookUserId(facebookUserId: String) =
            (facebookServiceLocatorActor ? ("facebookUserId", facebookUserId)).mapTo[FacebookService]
}
