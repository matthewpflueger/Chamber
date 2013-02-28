package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.public.EchoedUserPublic
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.domain.views.content.ContentDescription

case class PersonalizedContext(
    contentType:    ContentDescription,
    stats:          List[Map[String, Any]],
    highlights:     List[Map[String, Any]],
    content:        List[Map[String, Any]]) extends Context {

    val id =            "feed"
    val title =         "Content From People and Places You Follow"
    val contextType =   "me"

}
