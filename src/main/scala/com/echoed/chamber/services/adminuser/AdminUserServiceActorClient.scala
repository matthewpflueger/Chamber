package com.echoed.chamber.services.adminuser

import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient
import com.echoed.chamber.domain.partner.PartnerSettings
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._

class AdminUserServiceActorClient(adminUserServiceActor: ActorRef)
        extends AdminUserService
        with ActorClient
        with Serializable {

    def actorRef = adminUserServiceActor

    private implicit val timeout = Timeout(20 seconds)
    
    def getUsers =
        (adminUserServiceActor ? GetUsers()).mapTo[GetUsersResponse]

    def getPartners =
        (adminUserServiceActor ? GetPartners()).mapTo[GetPartnersResponse]

    def getPartner(partnerId: String) =
        (adminUserServiceActor ? GetPartner(partnerId)).mapTo[GetPartnerResponse]

    def getPartnerSettings(partnerId: String) =
        (adminUserServiceActor ? GetPartnerSettings(partnerId)).mapTo[GetPartnerSettingsResponse]

    def getCurrentPartnerSetting(partnerId: String) =
        (adminUserServiceActor ? GetCurrentPartnerSettings(partnerId)).mapTo[GetCurrentPartnerSettingsResponse]
    
    def getEchoPossibilities =
        (adminUserServiceActor ? GetEchoPossibilities()).mapTo[GetEchoPossibilitesResponse]

    def getAdminUser =
        (adminUserServiceActor ? GetAdminUser()).mapTo[GetAdminUserResponse]

    def updatePartnerSettings(partnerSettings: PartnerSettings) =
        (adminUserServiceActor ? UpdatePartnerSettings(partnerSettings)).mapTo[UpdatePartnerSettingsResponse]

    def updatePartnerHandleAndCategory(partnerId: String, partnerHandle: String, partnerCategory: String) =
        (adminUserServiceActor ? UpdatePartnerHandleAndCategory(partnerId, partnerHandle, partnerCategory)).mapTo[UpdatePartnerHandleAndCategoryResponse]

    def logout(adminUserId: String) =
        (adminUserServiceActor ? Logout(adminUserId)).mapTo[LogoutResponse]

    val id = actorRef.toString

    override def toString = id

}
