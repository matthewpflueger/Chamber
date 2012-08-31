package com.echoed.chamber.domain

import java.util.Date
import com.echoed.util.UUID
import com.echoed.util.DateUtils._
import scala.util.control.Exception._


case class EchoedUserSettings(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        echoedUser: EchoedUser,
        receiveNotificationEmail: Boolean) extends DomainObject {

    def this(echoedUser: EchoedUser, receiveNotificationEmail: Boolean = true) = this(
        UUID(),
        new Date,
        new Date,
        echoedUser,
        receiveNotificationEmail)

    def fromMap(map: Map[String, AnyRef]) = copy(
            receiveNotificationEmail = map
                .get("receiveNotificationEmail")
                .map(v => failAsValue(classOf[NumberFormatException])(receiveNotificationEmail) { v.toString.toBoolean })
                .getOrElse(receiveNotificationEmail))
}


