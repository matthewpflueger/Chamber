package com.echoed.chamber.services.partner.magentogo

import reflect.BeanProperty
import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient
import com.echoed.chamber.domain.partner.magentogo.MagentoGoCredentials


class MagentoGoAccessActorClient extends MagentoGoAccess with ActorClient with Serializable {

    @BeanProperty var magentoGoAccessActor: ActorRef = _

    def actorRef = magentoGoAccessActor

    def validate(credentials: MagentoGoCredentials) = (actorRef ? Validate(credentials)).mapTo[ValidateResponse]

    def fetchOrder(credentials: MagentoGoCredentials, order: Long) =
        (actorRef ? FetchOrder(credentials, order)).mapTo[FetchOrderResponse]

}
