package com.echoed.chamber.domain.views

import java.util.{ArrayList, List => JList}
import com.echoed.chamber.domain.EchoedUser


case class Feed(
        id: String, 
        echoedUser: EchoedUser,
        echoes: JList[EchoViewDetail],
        stories: JList[StoryFull]) {
    
    def this(id:String, echoedUser: EchoedUser) = this(id, echoedUser, new ArrayList[EchoViewDetail], new ArrayList[StoryFull])

    def this(id:String, echoedUser: EchoedUser, echoes: JList[EchoViewDetail]) = this(id, echoedUser, echoes, new ArrayList[StoryFull])


}

