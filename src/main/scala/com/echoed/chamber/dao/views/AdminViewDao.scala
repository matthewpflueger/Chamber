package com.echoed.chamber.dao.views

import java.util.{List => JList}
import com.echoed.chamber.domain.{Echo, EchoedUser}
import com.echoed.chamber.domain.partner.{PartnerSettings, Partner}
import org.apache.ibatis.annotations.Param


trait AdminViewDao {

    def getUsers: JList[EchoedUser]

    def getPartners: JList[Partner]
    
    def getPartnerSettings(
        @Param("partnerId") partnerId: String): JList[PartnerSettings]

    def getCurrentPartnerSettings(
        @Param("partnerId") partnerId: String): PartnerSettings

    def getEchoPossibilities: JList[Echo]

}
