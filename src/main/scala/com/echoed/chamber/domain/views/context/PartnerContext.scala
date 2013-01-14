package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.public.PartnerPublic
import com.echoed.chamber.domain.partner.Partner

case class PartnerContext(
    partner: PartnerPublic,
    followers: Int,
    stories: Int,
    views: Int,
    votes: Int
) extends Context {

    val id = partner.id
    val title = partner.name
    val contextType = "Partner"

    def this(partner: Partner,
             followers: Int,
             stories: Int,
             views: Int,
             votes: Int) = this(new PartnerPublic(partner), followers, stories, views, votes)

}

