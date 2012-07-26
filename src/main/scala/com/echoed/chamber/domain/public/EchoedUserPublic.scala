package com.echoed.chamber.domain.public

import com.echoed.chamber.domain.EchoedUser

case class EchoedUserPublic(
        id: String,
        name: String,
        screenName: String,
        facebookId: String,
        twitterId: String) {

    def this(echoedUser: EchoedUser) = this(
        echoedUser.id,
        echoedUser.name,
        echoedUser.screenName,
        echoedUser.facebookId,
        echoedUser.twitterId
    )

}
