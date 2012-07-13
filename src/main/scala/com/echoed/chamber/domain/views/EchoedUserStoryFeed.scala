package com.echoed.chamber.domain.views

import java.util.{ArrayList, List => JList}
import com.echoed.chamber.domain.public.EchoedUserPublic


case class EchoedUserStoryFeed(
    echoedUser: EchoedUserPublic,
    stories: JList[StoryFull]) {

    def this(echoedUser: EchoedUserPublic) = this(echoedUser, new ArrayList[StoryFull])

}
