package com.echoed.chamber.domain.views

import java.util.{ArrayList, Collection=>JCollection}
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.domain.public.StoryPublic


case class ClosetPersonal(
        id: String,
        echoedUser: EchoedUser,
        echoes: JCollection[EchoViewPersonal],
        stories: JCollection[StoryPublic],
        totalCredit: Float) {
    
    def this(id:String,  echoedUser: EchoedUser) = this(
            id,
            echoedUser,
            new ArrayList[EchoViewPersonal](),
            new ArrayList[StoryPublic],
            0f)

    def this(closet: Closet) = this(
            closet.id,
            closet.echoedUser,
            closet.convertEchoesToEchoViewPersonal,
            closet.stories,
            closet.totalCredit)
}
