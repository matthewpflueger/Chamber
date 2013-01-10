package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.Topic
import com.echoed.chamber.domain.public.PartnerPublic

case class TopicContext(
    topic: Topic,
    partner: PartnerPublic) extends Context{

    val id = topic.id
    val title = topic.title
    val contextType = "Topic"

}
