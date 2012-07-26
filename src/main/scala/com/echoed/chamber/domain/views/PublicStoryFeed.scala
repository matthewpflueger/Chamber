package com.echoed.chamber.domain.views

import com.echoed.chamber.domain.public.StoryPublic

case class PublicStoryFeed(stories: List[StoryPublic]) {

    def this() = this(List[StoryPublic]())

}