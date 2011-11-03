package com.echoed.chamber.services.facebook

import akka.dispatch.Future
import reflect.BeanProperty
import akka.actor.{ActorRef, TypedActor}


class FacebookServiceLocatorActorClient extends FacebookServiceLocator {

    @BeanProperty var actorRef: ActorRef = null

    def getFacebookServiceWithCode(code: String) = {
        Future[FacebookService]{
            (actorRef ? ("code", code)).get.asInstanceOf[FacebookService]
        }
    }

}