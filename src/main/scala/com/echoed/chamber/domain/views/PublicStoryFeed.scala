package com.echoed.chamber.domain.views

import com.echoed.chamber.domain.public.StoryPublic
import com.echoed.chamber.domain.Image

case class PublicStoryFeed(
        headerImage: Image,
        stories: List[StoryPublic],
        nextPage: String) {

    def this() = this(null, List[StoryPublic](), null)

}