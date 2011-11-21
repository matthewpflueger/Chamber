package com.echoed.chamber.domain.views

import java.util.{ArrayList, List}
import com.echoed.chamber.domain.{Echo, EchoedUser}


case class Closet(
        id: String,
        echoedUser: EchoedUser,
        var echoes: List[Echo]) {

    def this(id: String, echoedUser: EchoedUser) = this(id, echoedUser, new ArrayList[Echo]())

}
