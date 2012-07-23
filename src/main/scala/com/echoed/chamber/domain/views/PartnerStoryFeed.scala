package com.echoed.chamber.domain.views

import java.util.{ArrayList, List => JList}
import com.echoed.chamber.domain.public.PartnerPublic

case class PartnerStoryFeed(
    partner: PartnerPublic,
    stories: JList[StoryFull]) {

    def this(partner: PartnerPublic)= this(partner, new ArrayList[StoryFull])

}
