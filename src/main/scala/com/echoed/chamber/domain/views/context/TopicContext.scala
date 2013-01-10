package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.Topic
import com.echoed.chamber.domain.public.PartnerPublic

case class TopicContext(
    topic: Topic,
    partner: PartnerPublic) extends Context(topic.id, topic.title, "Topic")
