package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.views.content.ContentDescription

case class PublicContext(
    contentType:    ContentDescription,
    content:        List[Map[String, Any]]) extends Context{

    val id =            null
    val title =         "Explore"
    val contextType =   "public"
    val stats =         null
    val highlights =    null

}
