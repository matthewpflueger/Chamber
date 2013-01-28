package com.echoed.chamber.domain.public

import com.echoed.chamber.domain.{Identifiable, EchoedUser}

case class EchoedUserPublic(
        id: String,
        name: String,
        screenName: String,
        facebookId: String,
        twitterId: String) extends Identifiable {

    def this(echoedUser: EchoedUser) = this(
        echoedUser.id,
        echoedUser.name,
        echoedUser.screenName,
        echoedUser.facebookId,
        echoedUser.twitterId
    )

}
