package com.echoed.chamber.services.partneruser

import reflect.BeanProperty
import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._


class PartnerUserServiceLocatorActorClient extends PartnerUserServiceLocator with ActorClient with Serializable {

    @BeanProperty var partnerUserServiceLocatorActor: ActorRef = _

    private implicit val timeout = Timeout(20 seconds)

    def login(email: String, password: String) =
        (partnerUserServiceLocatorActor ? Login(email, password)).mapTo[LoginResponse]

    def logout(id: String) =
        (partnerUserServiceLocatorActor ? Logout(id)).mapTo[LogoutResponse]

    def locate(partnerUserId: String) =
        (partnerUserServiceLocatorActor ? Locate(partnerUserId)).mapTo[LocateResponse]

    def actorRef = partnerUserServiceLocatorActor


}
