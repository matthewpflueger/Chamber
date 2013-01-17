package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.public.{StoryPublic, PartnerPublic}
import com.echoed.chamber.domain.partner.Partner
import com.echoed.chamber.domain.views.content.Content

case class PartnerContext(
    partner:        PartnerPublic,
    stats:          List[Map[String, Any]],
    highlights:     List[Map[String, Any]],
    content:        List[Map[String, Any]]) extends Context {

    val id =            partner.id
    val title =         partner.name
    val contextType =   "Partner"

    def this(
        partner:        Partner,
        stats:          List[Map[String, Any]],
        highlights:     List[Map[String, Any]],
        content:        List[Map[String, Any]]) = this(new PartnerPublic(partner), stats, highlights, content )

}

