package com.echoed.chamber.services.partneruser

import reflect.BeanProperty
import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient


class PartnerUserServiceLocatorActorClient extends PartnerUserServiceLocator with ActorClient {

    @BeanProperty var partnerUserServiceLocatorActor: ActorRef = _

    def login(email: String, password: String) =
        (partnerUserServiceLocatorActor ? Login(email, password)).mapTo[LoginResponse]

    def logout(id: String) =
        (partnerUserServiceLocatorActor ? Logout(id)).mapTo[LogoutResponse]

    def locate(partnerUserId: String) =
        (partnerUserServiceLocatorActor ? Locate(partnerUserId)).mapTo[LocateResponse]

    def actorRef = partnerUserServiceLocatorActor


}
