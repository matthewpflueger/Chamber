package com.echoed.chamber.dao

import com.echoed.chamber.domain.PartnerSettings
import java.util.{Date, List => JList}
import org.apache.ibatis.annotations.Param


trait PartnerSettingsDao {

    def findById(id: String): PartnerSettings

    def findByPartnerId(partnerId: String): JList[PartnerSettings]

    def findByActiveOn(
            @Param("partnerId") partnerId: String,
            @Param("activeOn") activeOn: Date): PartnerSettings

    def findInactive(
            @Param("partnerId") partnerId: String,
            @Param("activeOn") activeOn: Date): PartnerSettings

    def insert(partnerSettings: PartnerSettings): Int

    def deleteByPartnerId(partnerId: String): Int

    def deleteById(id: String): Int
}




