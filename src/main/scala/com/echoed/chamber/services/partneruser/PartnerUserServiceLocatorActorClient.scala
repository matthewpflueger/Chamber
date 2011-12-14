package com.echoed.chamber.services.partneruser

import reflect.BeanProperty
import akka.actor.ActorRef


class PartnerUserServiceLocatorActorClient extends PartnerUserServiceLocator {

    @BeanProperty var partnerUserServiceLocatorActor: ActorRef = _

    def login(email: String, password: String) =
        (partnerUserServiceLocatorActor ? Login(email, password)).mapTo[LoginResponse]

    def locate(partnerUserId: String) =
        (partnerUserServiceLocatorActor ? Locate(partnerUserId)).mapTo[LocateResponse]
}
