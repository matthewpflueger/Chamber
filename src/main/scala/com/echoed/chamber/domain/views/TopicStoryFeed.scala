package com.echoed.chamber.domain.views

import com.echoed.chamber.domain.Topic
import com.echoed.chamber.domain.public.StoryPublic

case class TopicStoryFeed(
    topic: Topic,
    stories: List[StoryPublic],
    nextPage: String){

    def this(topic: Topic) = this(topic, List[StoryPublic](), null)
    def this(topic: Topic, feed: PublicStoryFeed) = this(topic, feed.stories, feed.nextPage)
}