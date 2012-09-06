package com.echoed.chamber.services.partneruser

import com.echoed.chamber.services.{UpdatedEvent, Event}
import com.echoed.chamber.domain.partner.PartnerUser


trait PartnerUserEvent extends Event

import com.echoed.chamber.services.partneruser.{PartnerUserEvent => PUE}


private[services] case class PartnerUserUpdated(partnerUser: PartnerUser)extends PUE with UpdatedEvent


