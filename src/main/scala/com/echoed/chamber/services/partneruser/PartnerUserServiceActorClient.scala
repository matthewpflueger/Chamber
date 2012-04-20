package com.echoed.chamber.services.partneruser

import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient


class PartnerUserServiceActorClient(partnerUserServiceActor: ActorRef) extends PartnerUserService with ActorClient {

    def activate(password: String) =
        (partnerUserServiceActor ? ActivatePartnerUser(password)).mapTo[ActivatePartnerUserResponse]

    def getPartnerUser =
        (partnerUserServiceActor ? GetPartnerUser()).mapTo[GetPartnerUserResponse]

    def getCustomerSocialSummary(echoedUserId: String) =
        (partnerUserServiceActor ? GetCustomerSocialSummary(echoedUserId)).mapTo[GetCustomerSocialSummaryResponse]

    def getCustomerSocialActivityByDate(echoedUserId: String) =
        (partnerUserServiceActor ? GetCustomerSocialActivityByDate(echoedUserId)).mapTo[GetCustomerSocialActivityByDateResponse]

    def getPartnerSocialSummary =
        (partnerUserServiceActor ? GetPartnerSocialSummary()).mapTo[GetPartnerSocialSummaryResponse]

    def getPartnerSocialActivityByDate =
        (partnerUserServiceActor ? GetPartnerSocialActivityByDate()).mapTo[GetPartnerSocialActivityByDateResponse]

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

    def getComments =
        (partnerUserServiceActor ? GetComments()).mapTo[GetCommentsResponse]

    def getCommentsByProductId(productId: String) =
        (partnerUserServiceActor ? GetCommentsByProductId(productId: String)).mapTo[GetCommentsByProductIdResponse]

    def getEchoClickGeoLocation =
        (partnerUserServiceActor ? GetEchoClickGeoLocation()).mapTo[GetEchoClickGeoLocationResponse]
    
    def getEchoes = 
        (partnerUserServiceActor ? GetEchoes()).mapTo[GetEchoesResponse]


    def logout(partnerUserId: String) =
        (partnerUserServiceActor ? Logout(partnerUserId)).mapTo[LogoutResponse]

    def actorRef = partnerUserServiceActor

    val id = actorRef.id

    override def toString = id
}
