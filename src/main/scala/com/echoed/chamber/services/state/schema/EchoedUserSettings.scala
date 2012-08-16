package com.echoed.chamber.services.state.schema

import com.echoed.chamber.domain
import org.squeryl.KeyedEntity
import com.echoed.chamber.domain.EchoedUser


case class EchoedUserSettings(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        echoedUserId: String,
        receiveNotificationEmail: Boolean) extends KeyedEntity[String] {

    def this() = this("", 0L, 0L, "", true)

    def convertTo(eu: EchoedUser) = domain.EchoedUserSettings(
            id,
            updatedOn,
            createdOn,
            eu,
            receiveNotificationEmail)

}

private[state] object EchoedUserSettings {
    def apply(eus: domain.EchoedUserSettings): EchoedUserSettings = EchoedUserSettings(
            eus.id,
            eus.updatedOn,
            eus.createdOn,
            eus.echoedUser.id,
            eus.receiveNotificationEmail)
}

