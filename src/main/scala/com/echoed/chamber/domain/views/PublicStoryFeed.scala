package com.echoed.chamber.domain.views
import java.util.{ArrayList, List => JList}

case class PublicStoryFeed(stories: JList[StoryFull]) {

    def this() = this(new ArrayList[StoryFull])

}