package com.echoed.chamber.domain.views
import java.util.{ArrayList, List => JList}

import com.echoed.chamber.domain.public.EchoedUserPublic
import java.util

case class EchoedUserFeed(
    echoedUser: EchoedUserPublic,
    echoes: JList[EchoViewPublic],
    stories: JList[StoryFull]) {

    def this(echoedUser: EchoedUserPublic)= this(echoedUser, new ArrayList[EchoViewPublic], new util.ArrayList[StoryFull])

    def this(echoedUser: EchoedUserPublic, echoes: JList[EchoViewPublic]) = this(echoedUser, echoes, new ArrayList[StoryFull])

}
