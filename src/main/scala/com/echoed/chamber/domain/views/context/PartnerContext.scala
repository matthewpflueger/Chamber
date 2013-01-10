package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.public.PartnerPublic
import com.echoed.chamber.domain.partner.Partner

case class PartnerContext(
    partner: PartnerPublic,
    followers: Int,
    stories: Int
) extends Context(partner.id, partner.name, "partner") {

    def this(partner: Partner, followers: Int, stories: Int) = this(new PartnerPublic(partner), followers, stories)

}

