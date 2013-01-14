package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.public.EchoedUserPublic
import com.echoed.chamber.domain.EchoedUser

case class UserContext(
    echoedUser: EchoedUserPublic,
    followers: Int,
    following: Int,
    stories: Int,
    views: Int,
    votes: Int
) extends Context {

    val id = echoedUser.id
    val title = echoedUser.name
    val contextType = "User"

    def this(echoedUser: EchoedUser, followers: Int, following: Int, stories: Int, views: Int, votes: Int) = this( new EchoedUserPublic(echoedUser), followers, following, stories, views, votes)

}
