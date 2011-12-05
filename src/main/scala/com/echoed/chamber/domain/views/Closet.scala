package com.echoed.chamber.domain.views

import java.util.{ArrayList, List => JList}
import com.echoed.chamber.domain.{Echo, EchoedUser}


case class Closet(
        id: String,
        echoedUser: EchoedUser,
        echoes: JList[Echo]) {

    def this(id: String, echoedUser: EchoedUser) = this(id, echoedUser, new ArrayList[Echo]())

}
