package com.echoed.chamber.domain.magentogo

import java.util.{UUID, Date}



case class MagentoGoPartner(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        name: String,
        email: String,
        phone: String,
        storeUrl: String,
        businessName: String,
        apiUser: String,
        apiPath: String,
        apiKey: String,
        partnerId: String) {

    def this(
        name: String,
        email: String,
        phone: String,
        storeUrl: String,
        businessName: String,
        apiUser: String,
        apiPath: String,
        apiKey: String) = this(
            UUID.randomUUID().toString,
            new Date,
            new Date,
            name,
            email,
            phone,
            storeUrl,
            businessName,
            apiUser,
            apiPath,
            apiKey,
            null)

    val credentials = MagentoGoCredentials(apiPath, apiUser, apiKey)
}


