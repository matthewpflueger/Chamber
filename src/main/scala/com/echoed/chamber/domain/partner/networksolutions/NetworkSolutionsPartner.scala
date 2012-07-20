package com.echoed.chamber.domain.partner.networksolutions

import java.util.Date
import com.echoed.util.UUID


case class NetworkSolutionsPartner(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        name: String,
        email: String,
        phone: String,
        userKey: String,
        userToken: String,
        userTokenExpiresOn: Date,
        storeUrl: String,
        secureStoreUrl: String,
        companyName: String,
        partnerId: String) {

    def this(name: String, email: String, phone: String, userKey: String) = this(
        UUID(),
        new Date,
        new Date,
        name,
        email,
        phone,
        userKey,
        null,
        null,
        null,
        null,
        null,
        null)
}



