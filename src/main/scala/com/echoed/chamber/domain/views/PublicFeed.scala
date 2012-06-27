package com.echoed.chamber.domain.views
import java.util.{ArrayList, List => JList}
import java.util


case class PublicFeed(
    echoes: JList[EchoViewPublic],
    stories: JList[StoryFull]) {

    def this() = this(new ArrayList[EchoViewPublic], new ArrayList[StoryFull])

    def this(echoes: JList[EchoViewPublic]) = this(echoes, new util.ArrayList[StoryFull])
}
