package com.echoed.chamber.domain.public

import com.echoed.chamber.domain.Topic

case class TopicPublic(
    id: String,
    title: String,
    description: Option[String]) {

    def this(topic: Topic) = this(topic.id, topic.title, topic.description)

}
