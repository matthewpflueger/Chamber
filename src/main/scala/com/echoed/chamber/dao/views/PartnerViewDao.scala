package com.echoed.chamber.dao.views


import org.apache.ibatis.annotations.Param
import java.util.{List => JList}
import com.echoed.chamber.domain.views._
import com.echoed.chamber.domain.{GeoLocation, FacebookComment, EchoedUser}
import com.echoed.chamber.domain.partner.PartnerSettings


trait PartnerViewDao {

    def getPartnerSettings(
            @Param("partnerId") partnerId: String): JList[PartnerSettings]
    
}