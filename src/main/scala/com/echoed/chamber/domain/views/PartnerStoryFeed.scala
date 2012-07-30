package com.echoed.chamber.domain.views

import java.util.{ArrayList, List => JList}
import com.echoed.chamber.domain.public.{StoryPublic, PartnerPublic}

case class PartnerStoryFeed(
    partner: PartnerPublic,
    stories: JList[StoryPublic],
    nextPage: String) {

    def this(partner: PartnerPublic)= this(partner, new ArrayList[StoryPublic], null)

}
