package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.public.EchoedUserPublic
import com.echoed.chamber.domain.EchoedUser

case class SelfContext( echoedUser: EchoedUserPublic) extends Context {

    val id =            echoedUser.id
    val title =         "My Content"
    val contextType =   "me"

    def this( echoedUser: EchoedUser ) = this( new EchoedUserPublic( echoedUser ) )

}
