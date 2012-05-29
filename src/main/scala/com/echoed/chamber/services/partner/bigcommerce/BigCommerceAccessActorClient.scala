package com.echoed.chamber.services.partner.bigcommerce

import reflect.BeanProperty
import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient
import com.echoed.chamber.domain.partner.bigcommerce.BigCommerceCredentials


class BigCommerceAccessActorClient extends BigCommerceAccess with ActorClient with Serializable {

    @BeanProperty var bigCommerceAccessActor: ActorRef = _

    def actorRef = bigCommerceAccessActor

    def validate(credentials: BigCommerceCredentials) = (actorRef ? Validate(credentials)).mapTo[ValidateResponse]

    def fetchOrder(credentials: BigCommerceCredentials, order: Long) =
        (actorRef ? FetchOrder(credentials, order)).mapTo[FetchOrderResponse]

}
