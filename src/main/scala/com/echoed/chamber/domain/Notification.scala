package com.echoed.chamber.domain

import com.echoed.util.UUID
import java.util.Date
import com.echoed.util.DateUtils._


case class Notification(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        echoedUserId: String,
        origin: Identifiable,
        category: String,
        value: Map[String, String],
        emailedOn: Option[Long] = None,
        readOn: Option[Long] = None,
        notificationType: Option[String] = None) extends DomainObject {

    def this(
            origin: Identifiable,
            category: String,
            value: Map[String, String],
            notificationType: Option[String] = None) = this(
        UUID(),
        new Date,
        new Date,
        echoedUserId,
        origin,
        category,
        value,
        None,
        None,
        notificationType)

    def hasRead = readOn.isDefined
    def markAsRead = copy(readOn = new Date)

    def hasEmailed = emailedOn.isDefined
    def markAsEmailed = copy(emailedOn = new Date)

    def canEmail = !hasRead && !hasEmailed
    def isWeekly = notificationType.filter(_ == "weekly").isDefined
}


