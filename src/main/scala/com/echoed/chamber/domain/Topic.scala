package com.echoed.chamber.domain

import com.echoed.chamber.domain.partner.Partner
import java.util.Date
import com.echoed.util.UUID
import com.echoed.util.DateUtils._

case class Topic(
        id: String,
        createdOn: Long,
        updatedOn: Long,
        partnerId: String,
        community: String,
        title: String,
        description: Option[String],
        beginOn: Long,
        endOn: Long) extends DomainObject {

    def this() = this("", 0L, 0L, "", "", "", Some(""), 0L, 0L)

    def this(
            partner: Partner,
            title: String,
            description: Option[String] = None,
            beginOn: Option[Date] = None,
            endOn: Option[Date] = None) = this(
        UUID(),
        new Date,
        new Date,
        partner.id,
        partner.category,
        title,
        description,
        beginOn.map(dateToLong(_)).getOrElse(new Date),
        endOn.map(dateToLong(_)).getOrElse(maxDate))
}
