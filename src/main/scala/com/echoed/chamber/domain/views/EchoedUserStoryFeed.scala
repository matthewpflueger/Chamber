package com.echoed.chamber.domain.views

import java.util.{ArrayList, List => JList}
import com.echoed.chamber.domain.public.{StoryPublic, EchoedUserPublic}


case class EchoedUserStoryFeed(
    echoedUser: EchoedUserPublic,
    stories: JList[StoryPublic],
    nextPage: String) {

    def this(echoedUser: EchoedUserPublic) = this(echoedUser, new ArrayList[StoryPublic], null)

}
