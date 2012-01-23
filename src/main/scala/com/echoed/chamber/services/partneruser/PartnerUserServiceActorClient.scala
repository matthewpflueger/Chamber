package com.echoed.chamber.services.partneruser

import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient


class PartnerUserServiceActorClient(partnerUserServiceActor: ActorRef) extends PartnerUserService with ActorClient {

    def getPartnerUser =
        (partnerUserServiceActor ? GetPartnerUser()).mapTo[GetPartnerUserResponse]

    def getRetailerSocialSummary =
        (partnerUserServiceActor ? GetRetailerSocialSummary()).mapTo[GetRetailerSocialSummaryResponse]

    def getProductSocialSummary(productId: String) =
        (partnerUserServiceActor ? GetProductSocialSummary(productId)).mapTo[GetProductSocialSummaryResponse]
    
    def getProductSocialActivityByDate(productId:String) =
        (partnerUserServiceActor ? GetProductSocialActivityByDate(productId)).mapTo[GetProductSocialActivityByDateResponse]

    def getTopProducts =
        (partnerUserServiceActor ? GetTopProducts()).mapTo[GetTopProductsResponse]

    def getTopCustomers = 
        (partnerUserServiceActor ? GetTopCustomers()).mapTo[GetTopCustomersResponse]

    def logout(partnerUserId: String) =
        (partnerUserServiceActor ? Logout(partnerUserId)).mapTo[LogoutResponse]

    def actorRef = partnerUserServiceActor

    val id = actorRef.id

    override def toString = id
}
