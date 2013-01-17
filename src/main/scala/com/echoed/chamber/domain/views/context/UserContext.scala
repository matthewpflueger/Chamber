package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.public.{StoryPublic, EchoedUserPublic}
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.domain.views.content.Content

case class UserContext(
    echoedUser:     EchoedUserPublic,
    stats:          List[Map[String, Any]],
    highlights:     List[Map[String, Any]],
    content:        List[Map[String, Any]]) extends Context {

    val id =            echoedUser.id
    val title =         echoedUser.name
    val contextType =   "User"

    def this(echoedUser:    EchoedUser,
             stats:         List[Map[String, Any]],
             highlights:     List[Map[String, Any]],
             content:       List[Map[String, Any]]) = this( new EchoedUserPublic(echoedUser), stats, highlights, content )

}
