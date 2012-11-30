package com.echoed.chamber.domain.views

import com.echoed.chamber.domain.Image
import com.echoed.chamber.domain.partner.Partner
import com.echoed.chamber.domain.public.{StoryPublic, PartnerPublic}

case class PartnerStoryFeed(
    partner: PartnerPublic,
    headerImage: Image,
    stories: List[StoryPublic],
    nextPage: String) {

    def this(partner: Partner, feed: PublicStoryFeed) = this(new PartnerPublic(partner), feed.headerImage, feed.stories, feed.nextPage)
    def this(partner: PartnerPublic)= this(partner, null, List[StoryPublic](), null)


}
