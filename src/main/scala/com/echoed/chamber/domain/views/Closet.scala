package com.echoed.chamber.domain.views

import java.util.{ArrayList, List => JList}
import com.echoed.chamber.domain.EchoedUser


case class Closet(
        id: String,
        echoedUser: EchoedUser,
        echoes: JList[EchoView]) {

    def this(id: String, echoedUser: EchoedUser) = this(id, echoedUser, new ArrayList[EchoView]())

}
