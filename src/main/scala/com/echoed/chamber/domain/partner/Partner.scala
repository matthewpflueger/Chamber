package com.echoed.chamber.domain.partner

import java.util.Date
import com.echoed.util.UUID


case class Partner(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        name: String,
        domain: String,
        phone: String,
        handle: String,
        logo: String,
        @transient secret: String,
        category: String,
        cloudPartnerId: String) {

    def this(
            name: String,
            domain: String,
            phone: String,
            handle: String,
            logo: String,
            category: String) = this(
        UUID(),
        new Date,
        new Date,
        name,
        domain,
        phone,
        handle,
        logo,
        UUID(),
        category,
        null)

    def this(name: String) = this(
        name,
        UUID(),
        UUID(),
        UUID(),
        UUID(),
        UUID())


    val isEchoed = "Echoed" == handle
}

