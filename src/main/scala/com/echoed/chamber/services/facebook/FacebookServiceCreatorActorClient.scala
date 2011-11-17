package com.echoed.chamber.services.facebook

import akka.actor.{Channel, ActorRef, Actor}
import reflect.BeanProperty
import akka.dispatch.{Promise, Future}


class FacebookServiceCreatorActorClient extends FacebookServiceCreator {

    @BeanProperty var facebookServiceCreatorActor: ActorRef = null

    def createFacebookServiceUsingCode(code: String) = {
        Future[FacebookService] {
            (facebookServiceCreatorActor ? ("code", code)).get.asInstanceOf[FacebookService]
        }
    }

    def createFacebookServiceUsingFacebookUserId(facebookUserId: String) =
        (facebookServiceCreatorActor ? ("facebookUserId", facebookUserId)).mapTo[FacebookService]
}
