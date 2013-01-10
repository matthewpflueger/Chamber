package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.public.EchoedUserPublic
import com.echoed.chamber.domain.EchoedUser

case class UserContext(
    echoedUser: EchoedUserPublic,
    followers: Int,
    following: Int,
    stories: Int
) extends Context(echoedUser.id, echoedUser.name, "User") {

    def this(echoedUser: EchoedUser, followers: Int, following: Int, stories: Int) = this( new EchoedUserPublic(echoedUser), followers, following, stories)

}
