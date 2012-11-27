package com.echoed.chamber.services.state.schema

import org.squeryl.KeyedEntity
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.services.echoeduser
import com.echoed.chamber.domain.partner.Partner


private[state] case class Follower(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        ref: String,
        refId: String,
        echoedUserId: String) extends KeyedEntity[String] {

    def this() = this("", 0L, 0L, "", "", "")

    def convertTo(eu: EchoedUser) = echoeduser.Follower(eu.id, eu.name, eu.screenName, eu.facebookId, eu.twitterId)

    def convertToPartnerFollower(p: Partner) = echoeduser.PartnerFollower(p.id, p.name, p.handle)
}
