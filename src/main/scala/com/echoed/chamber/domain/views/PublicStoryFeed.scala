package com.echoed.chamber.domain.views

case class PublicStoryFeed(stories: List[StoryFull]) {

    def this() = this(List[StoryFull]())

}