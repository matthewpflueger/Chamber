package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.public.EchoedUserPublic
import com.echoed.chamber.domain.EchoedUser

case class SelfContext(
      echoedUser:     EchoedUserPublic,
      stats:          List[Map[String, Any]],
      highlights:     List[Map[String, Any]],
      content:        List[Map[String, Any]]) extends Context {

    val id =            echoedUser.id
    val title =         "My Content"
    val contextType =   "user"



    def this(echoedUser:    EchoedUser,
             stats:         List[Map[String, Any]],
             highlights:     List[Map[String, Any]],
             content:       List[Map[String, Any]]) = this( new EchoedUserPublic(echoedUser), stats, highlights, content )

}
