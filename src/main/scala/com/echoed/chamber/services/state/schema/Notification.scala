package com.echoed.chamber.services.state.schema

import com.echoed.chamber.domain
import com.echoed.util.ScalaObjectMapper
import com.echoed.chamber.domain.{EchoedUser, Identifiable}
import org.squeryl._

private[state] case class Notification(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        echoedUserId: String,
        originId: String,
        category: String,
        value: String,
        emailedOn: Option[Long] = None,
        readOn: Option[Long] = None) extends KeyedEntity[String] {

    def this() = this(
            "",
            0L,
            0L,
            "",
            "",
            "",
            "",
            Some(0L),
            Some(0L))

    def convertTo(eu: EchoedUser) = domain.Notification(
            id,
            updatedOn,
            createdOn,
            eu,
            new Identifiable { val id = originId },
            category,
            ScalaObjectMapper(value, classOf[Map[String, String]]),
            emailedOn,
            readOn)
}


private[state] object Notification {
    def apply(n: domain.Notification): Notification = Notification(
            n.id,
            n.updatedOn,
            n.createdOn,
            n.echoedUser.id,
            n.origin.id,
            n.category,
            new ScalaObjectMapper().writeValueAsString(n.value),
            n.emailedOn,
            n.readOn)
}
