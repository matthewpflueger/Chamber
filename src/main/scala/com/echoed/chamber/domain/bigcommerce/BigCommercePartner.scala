package com.echoed.chamber.domain.bigcommerce

import java.util.{UUID, Date}



case class BigCommercePartner(
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
        apiToken: String,
        partnerId: String) {

    def this(
        name: String,
        email: String,
        phone: String,
        storeUrl: String,
        businessName: String,
        apiUser: String,
        apiPath: String,
        apiToken: String) = this(
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
            apiToken,
            null)

    val credentials = BigCommerceCredentials(apiPath, apiUser, apiToken)
}


