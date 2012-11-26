package com.echoed.chamber.domain.views

import com.echoed.chamber.domain.Topic
import com.echoed.chamber.domain.public.StoryPublic

case class TopicStoryFeed(
    topic: Topic,
    stories: List[StoryPublic],
    nextPage: String){

    def this(topic: Topic) = this(topic, List[StoryPublic](), null)
}