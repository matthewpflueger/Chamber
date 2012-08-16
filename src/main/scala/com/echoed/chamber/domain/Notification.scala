package com.echoed.chamber.domain

import com.echoed.util.UUID
import java.util.Date
import com.echoed.util.DateUtils._


case class Notification(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        echoedUser: EchoedUser,
        origin: Identifiable,
        category: String,
        value: Map[String, String],
        emailedOn: Option[Long] = None,
        readOn: Option[Long] = None) extends DomainObject {

    def this(echoedUser: EchoedUser, origin: Identifiable, category: String, value: Map[String, String]) = this(
            UUID(),
            new Date,
            new Date,
            echoedUser,
            origin,
            category,
            value)

    def hasRead = readOn.isDefined
    def markAsRead = copy(readOn = new Date)
}


