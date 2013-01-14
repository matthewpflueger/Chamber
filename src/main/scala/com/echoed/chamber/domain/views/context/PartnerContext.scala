package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.public.{StoryPublic, PartnerPublic}
import com.echoed.chamber.domain.partner.Partner

case class PartnerContext(
    partner: PartnerPublic,
    followers: Int,
    stories: Int,
    views: Int,
    votes: Int,
    comments: Int,
    mostCommented: StoryPublic,
    mostViewed: StoryPublic
) extends Context {

    val id = partner.id
    val title = partner.name
    val contextType = "Partner"

    def this(partner: Partner,
             followers: Int,
             stories: Int,
             views: Int,
             votes: Int,
             comments: Int,
             mostCommented: StoryPublic,
             mostViewed: StoryPublic) = this(new PartnerPublic(partner), followers, stories, views, votes, comments, mostCommented, mostViewed)

}

