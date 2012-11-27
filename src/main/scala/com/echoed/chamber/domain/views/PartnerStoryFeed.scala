package com.echoed.chamber.domain.views

import java.util.{ArrayList, List => JList}
import com.echoed.chamber.domain.public.{StoryPublic, PartnerPublic}
import com.echoed.chamber.domain.partner.Partner

case class PartnerStoryFeed(
    partner: PartnerPublic,
    headerImageUrl: String,
    stories: List[StoryPublic],
    nextPage: String) {

    def this(partner: Partner, feed: PublicStoryFeed) = this(new PartnerPublic(partner), feed.headerImageUrl, feed.stories, feed.nextPage)
    def this(partner: PartnerPublic)= this(partner, null, List[StoryPublic](), null)


}
