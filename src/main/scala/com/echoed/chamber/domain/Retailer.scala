package com.echoed.chamber.domain

import java.util.{UUID, Date}


case class Retailer(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        name: String,
        website: String,
        phone: String,
        hashTag: String,
        logo: String,
        secret: String,
        category: String) {


    def this(
            name: String,
            website: String,
            phone: String,
            hashTag: String,
            logo: String,
            category: String) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        name,
        website,
        phone,
        hashTag,
        logo,
        UUID.randomUUID.toString,
        category)

    def this(name: String) = this(
        name,
        UUID.randomUUID.toString,
        UUID.randomUUID.toString,
        UUID.randomUUID.toString,
        UUID.randomUUID.toString,
        UUID.randomUUID.toString)
}

