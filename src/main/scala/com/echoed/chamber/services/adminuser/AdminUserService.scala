package com.echoed.chamber.services.adminuser

import com.echoed.chamber.domain.AdminUser
import akka.dispatch.Future
import com.echoed.chamber.domain.partner.PartnerSettings

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/2/12
 * Time: 9:10 PM
 * To change this template use File | Settings | File Templates.
 */

trait AdminUserService {

    val id: String

    def getAdminUser: Future[GetAdminUserResponse]
    
    def getUsers: Future[GetUsersResponse]

    def getPartners: Future[GetPartnersResponse]

    def getPartner(partnerId: String): Future[GetPartnerResponse]

    def getPartnerSettings(partnerId: String): Future[GetPartnerSettingsResponse]

    def getCurrentPartnerSetting(partnerId: String): Future[GetCurrentPartnerSettingsResponse]

    def updatePartnerHandle(partnerId: String, partnerHandle: String): Future[UpdatePartnerHandleResponse]

    def updatePartnerSettings(partnerSettings: PartnerSettings): Future[UpdatePartnerSettingsResponse]
    
    def getEchoPossibilities: Future[GetEchoPossibilitesResponse]
    
    def logout(adminUserId: String): Future[LogoutResponse]

}
