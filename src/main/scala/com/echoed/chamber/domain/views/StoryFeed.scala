package com.echoed.chamber.domain.views

import context.Context
import com.echoed.chamber.domain.public.StoryPublic

case class StoryFeed(
    context: Context,
    stories: List[StoryPublic],
    nextPage: String) {

    def this() = this(null, List[StoryPublic](), null)

}
