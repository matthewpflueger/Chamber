package com.echoed.chamber.domain.partner

import java.util.{UUID, Date}


case class Partner(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        name: String,
        domain: String,
        phone: String,
        hashTag: String,
        logo: String,
        @transient secret: String,
        category: String,
        cloudPartnerId: String) {

    def this(
            name: String,
            domain: String,
            phone: String,
            hashTag: String,
            logo: String,
            category: String) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        name,
        domain,
        phone,
        hashTag,
        logo,
        UUID.randomUUID.toString,
        category,
        null)

    def this(name: String) = this(
        name,
        UUID.randomUUID.toString,
        UUID.randomUUID.toString,
        UUID.randomUUID.toString,
        UUID.randomUUID.toString,
        UUID.randomUUID.toString)


}

