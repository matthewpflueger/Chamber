package com.echoed.chamber.services.partner

import com.echoed.chamber.services.{CreatedEvent, Event}
import com.echoed.chamber.domain.partner.{PartnerUser, PartnerSettings, Partner}


trait PartnerEvent extends Event

import com.echoed.chamber.services.partner.{PartnerEvent => PE}

private[services] case class PartnerCreated(
        partner: Partner,
        partnerSettings: PartnerSettings,
        partnerUser: PartnerUser) extends PE with CreatedEvent
