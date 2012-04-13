package com.echoed.chamber.domain

import java.util.{UUID, Date}


//-- "66.202.133.170","66.202.133.170","US","United States","NY","New York","New York","","40.761900","-73.976300","Regus Business Center","Regus Business Center"

case class GeoLocation(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        ipAddress: String,
        countryCode: String,
        countryName: String,
        regionCode: String,
        regionName: String,
        city: String,
        postcode: String,
        latitude: String,
        longitude: String,
        isp: String,
        organization: String,
        updateStatus: String) {

    def this(
            ipAddress: String,
            countryCode: String = null,
            countryName: String = null,
            regionCode: String = null,
            regionName: String = null,
            city: String = null,
            postcode: String = null,
            latitude: String = null,
            longitude: String = null,
            isp: String = null,
            organization: String = null,
            updateStatus: String = null) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        ipAddress,
        countryCode,
        countryName,
        regionCode,
        regionName,
        city,
        postcode,
        latitude,
        longitude,
        isp,
        organization,
        updateStatus)

    require(ipAddress != null, "ipAddress is null")
}



