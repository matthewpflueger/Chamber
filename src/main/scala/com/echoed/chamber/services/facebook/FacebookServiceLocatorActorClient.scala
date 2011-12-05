package com.echoed.chamber.services.facebook

import reflect.BeanProperty
import akka.actor.ActorRef


class FacebookServiceLocatorActorClient extends FacebookServiceLocator {

    @BeanProperty var facebookServiceLocatorActor: ActorRef = _

    def getFacebookServiceWithCode(code: String, queryString: String) =
            (facebookServiceLocatorActor ? ("code", code, queryString)).mapTo[FacebookService]


    def getFacebookServiceWithFacebookUserId(facebookUserId: String) =
            (facebookServiceLocatorActor ? ("facebookUserId", facebookUserId)).mapTo[FacebookService]
}
