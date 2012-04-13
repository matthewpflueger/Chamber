package com.echoed.chamber.dao

import java.util.Date
import com.echoed.chamber.domain.GeoLocation
import org.apache.ibatis.annotations.Param


trait GeoLocationDao {

    def findById(id: String): GeoLocation

    def findByIpAddress(ipAddress: String): GeoLocation

    def findForCrawl(
            @Param("lastUpdatedOn") lastUpdatedOn: Date,
            @Param("findClick") findClick: Boolean): GeoLocation

    def insertOrUpdateForFailure(geoLocation: GeoLocation): Int

    def insertOrUpdate(geoLocation: GeoLocation): Int

    def deleteById(id: String): Int

    def deleteByIpAddress(ipAddress: String): Int

}
