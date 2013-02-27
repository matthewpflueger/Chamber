package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.views.content.ContentDescription

case class PublicContext(
    contentType:    ContentDescription) extends Context{

    val id =            null
    val title =         null
    val contextType =   "public"
    val content =       null
    val stats =         null
    val highlights =    null

}
