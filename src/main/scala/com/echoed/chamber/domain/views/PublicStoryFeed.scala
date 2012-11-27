package com.echoed.chamber.domain.views

import com.echoed.chamber.domain.public.StoryPublic

case class PublicStoryFeed(
        headerImageUrl: String,
        stories: List[StoryPublic],
        nextPage: String) {

    def this() = this(null, List[StoryPublic](), null)

}