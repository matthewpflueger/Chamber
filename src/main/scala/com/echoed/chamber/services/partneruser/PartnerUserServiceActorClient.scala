package com.echoed.chamber.services.partneruser

import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient


class PartnerUserServiceActorClient(partnerUserServiceActor: ActorRef) extends PartnerUserService with ActorClient {

    def getPartnerUser =
        (partnerUserServiceActor ? GetPartnerUser()).mapTo[GetPartnerUserResponse]
    
    def getCustomerSocialSummary(echoedUserId: String) =
        (partnerUserServiceActor ? GetCustomerSocialSummary(echoedUserId)).mapTo[GetCustomerSocialSummaryResponse]
    
    def getCustomerSocialActivityByDate(echoedUserId: String) =
        (partnerUserServiceActor ? GetCustomerSocialActivityByDate(echoedUserId)).mapTo[GetCustomerSocialActivityByDateResponse]

    def getRetailerSocialSummary =
        (partnerUserServiceActor ? GetRetailerSocialSummary()).mapTo[GetRetailerSocialSummaryResponse]
    
    def getRetailerSocialActivityByDate =
        (partnerUserServiceActor ? GetRetailerSocialActivityByDate()).mapTo[GetRetailerSocialActivityByDateResponse]

    def getProductSocialSummary(productId: String) =
        (partnerUserServiceActor ? GetProductSocialSummary(productId)).mapTo[GetProductSocialSummaryResponse]
    
    def getProductSocialActivityByDate(productId:String) =
        (partnerUserServiceActor ? GetProductSocialActivityByDate(productId)).mapTo[GetProductSocialActivityByDateResponse]

    def getProducts = 
        (partnerUserServiceActor ? GetProducts()).mapTo[GetProductsResponse]
    
    def getTopProducts =
        (partnerUserServiceActor ? GetTopProducts()).mapTo[GetTopProductsResponse]
    
    def getCustomers = 
        (partnerUserServiceActor ? GetCustomers()).mapTo[GetCustomersResponse]

    def getTopCustomers = 
        (partnerUserServiceActor ? GetTopCustomers()).mapTo[GetTopCustomersResponse]

    def logout(partnerUserId: String) =
        (partnerUserServiceActor ? Logout(partnerUserId)).mapTo[LogoutResponse]

    def actorRef = partnerUserServiceActor

    val id = actorRef.id

    override def toString = id
}
