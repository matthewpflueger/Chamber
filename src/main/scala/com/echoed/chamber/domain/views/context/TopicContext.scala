package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.Topic
import com.echoed.chamber.domain.public.PartnerPublic

case class TopicContext( topic: Topic ) extends Context{

    val id =            topic.id
    val title =         topic.title
    val contextType =   "Topic"
    val content =       null
    val stats =         null
    val highlights =    null

}
