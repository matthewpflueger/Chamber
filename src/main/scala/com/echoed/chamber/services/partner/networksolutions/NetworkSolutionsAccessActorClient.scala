package com.echoed.chamber.services.partner.networksolutions

import reflect.BeanProperty
import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient



class NetworkSolutionsAccessActorClient extends NetworkSolutionsAccess with ActorClient with Serializable {

    @BeanProperty var networkSolutionsAccessActor: ActorRef = _

    def actorRef = networkSolutionsAccessActor

    def fetchUserKey(successUrl: String, failureUrl: Option[String]) =
        (actorRef ? FetchUserKey(successUrl, failureUrl)).mapTo[FetchUserKeyResponse]

    def fetchUserToken(userKey: String) =
        (actorRef ? FetchUserToken(userKey)).mapTo[FetchUserTokenResponse]

    def fetchOrder(userToken: String, orderNumber: Long) =
        (actorRef ? FetchOrder(userToken, orderNumber)).mapTo[FetchOrderResponse]

}
