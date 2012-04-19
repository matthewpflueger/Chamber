package com.echoed.chamber.services.partner

import akka.dispatch.Future
import com.echoed.chamber.domain.{PartnerSettings, PartnerUser, Partner}


trait PartnerServiceManager {
    
    def updatePartnerSettings( partnerSettings: PartnerSettings ): Future[UpdatePartnerSettingsResponse]

    def registerPartner(
            partner: Partner,
            partnerSettings: PartnerSettings,
            partnerUser: PartnerUser): Future[RegisterPartnerResponse]

    def locatePartnerService(partnerId: String): Future[LocateResponse]

    def locatePartnerByEchoId(echoId: String): Future[LocateByEchoIdResponse]
}

