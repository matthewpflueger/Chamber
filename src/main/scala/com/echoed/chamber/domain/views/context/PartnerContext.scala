package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.public.{StoryPublic, PartnerPublic}
import com.echoed.chamber.domain.partner.Partner
import com.echoed.chamber.domain.views.content.{ContentDescription, Content}

case class PartnerContext(
    partner:        PartnerPublic,
    page:           Option[String] = None,
    contentType:    ContentDescription,
    stats:          List[Map[String, Any]],
    highlights:     List[Map[String, Any]],
    content:        List[Map[String, Any]]) extends Context {

    val id =            partner.id
    val title =         partner.name
    val contextType =   "partner"

    def this(
        partner:        Partner,
        page:           Option[String],
        contentType:    ContentDescription,
        stats:          List[Map[String, Any]],
        highlights:     List[Map[String, Any]],
        content:        List[Map[String, Any]]) = this(new PartnerPublic(partner), None, contentType, stats, highlights, content )

    def this(partner: Partner) = this(new PartnerPublic(partner), None, null, List(), List(), List())
}

