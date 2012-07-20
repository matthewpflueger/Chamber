package com.echoed.chamber.domain.partner.bigcommerce

import java.util.Date
import com.echoed.util.UUID


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
            UUID(),
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


