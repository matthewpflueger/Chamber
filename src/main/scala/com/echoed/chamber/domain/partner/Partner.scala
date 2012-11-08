package com.echoed.chamber.domain.partner

import java.util.Date
import com.echoed.util.UUID
import com.echoed.chamber.domain.DomainObject
import com.echoed.util.DateUtils._
import org.squeryl.annotations.Transient


case class Partner(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        name: String,
        domain: String,
        phone: String,
        handle: String,
        logo: String,
        @transient secret: String,
        category: String,
        cloudPartnerId: String) extends DomainObject {

    def this() = this("", 0L, 0L, "", "", "", "", "", "", "", "")

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

    def this(siteName: String, siteUrl: String, shortName: String, community: String) = this(
        siteName,
        siteUrl,
        UUID(),
        shortName,
        UUID(),
        community)



    @Transient val isEchoed = "Echoed" == handle
}

