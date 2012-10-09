package com.echoed.chamber.services.state.schema

import org.squeryl.KeyedEntity
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.services.echoeduser


private[state] case class Follower(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        ref: String,
        refId: String,
        echoedUserId: String) extends KeyedEntity[String] {

    def this() = this("", 0L, 0L, "", "", "")

    def convertTo(eu: EchoedUser) = echoeduser.Follower(eu.id, eu.name, Option(eu.facebookId), Option(eu.twitterId))
}
