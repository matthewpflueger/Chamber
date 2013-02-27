package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.public.EchoedUserPublic
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.domain.views.content.ContentDescription

case class SelfContext(
      echoedUser:     EchoedUserPublic,
      contentType:    ContentDescription,
      stats:          List[Map[String, Any]],
      highlights:     List[Map[String, Any]],
      content:        List[Map[String, Any]]) extends Context {

    val id =            echoedUser.id
    val title =         "My Content"
    val contextType =   "me"



    def this(
        echoedUser:     EchoedUser,
        contentType:    ContentDescription,
        stats:          List[Map[String, Any]],
        highlights:     List[Map[String, Any]],
        content:        List[Map[String, Any]]) = this( new EchoedUserPublic(echoedUser), contentType, stats, highlights, content )

}
