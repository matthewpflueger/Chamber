package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.public.EchoedUserPublic
import com.echoed.chamber.domain.EchoedUser

case class SelfContext( echoedUser: EchoedUserPublic ) extends Context {

    val id = echoedUser.id
    val title = "My Stuff"
    val contextType = "self"

    def this( echoedUser: EchoedUser ) = this( new EchoedUserPublic( echoedUser ) )

}
