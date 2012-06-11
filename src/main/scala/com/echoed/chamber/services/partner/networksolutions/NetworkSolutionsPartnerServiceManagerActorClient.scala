package com.echoed.chamber.services.partner.networksolutions

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.services.ActorClient
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._

class NetworkSolutionsPartnerServiceManagerActorClient
        extends NetworkSolutionsPartnerServiceManager
        with ActorClient {

    @BeanProperty var actorRef: ActorRef = _

    private implicit val timeout = Timeout(20 seconds)

    def registerPartner(name: String, email: String, phone: String, successUrl: String, failureUrl: Option[String]) =
        (actorRef ? RegisterNetworkSolutionsPartner(name, email, phone, successUrl, failureUrl)).mapTo[RegisterNetworkSolutionsPartnerResponse]

    def authPartner(userKey: String) =
        (actorRef ? AuthNetworkSolutionsPartner(userKey)).mapTo[AuthNetworkSolutionsPartnerResponse]
}
