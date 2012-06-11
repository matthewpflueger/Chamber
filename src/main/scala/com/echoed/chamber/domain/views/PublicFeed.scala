package com.echoed.chamber.domain.views
import java.util.{ArrayList, List => JList}


case class PublicFeed(echoes: JList[EchoViewPublic]) {

    def this() = this(new ArrayList[EchoViewPublic])
}
