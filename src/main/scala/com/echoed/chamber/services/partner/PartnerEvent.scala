package com.echoed.chamber.services.partner

import com.echoed.chamber.services.{UpdatedEvent, CreatedEvent, Event}
import com.echoed.chamber.domain.partner.{PartnerUser, PartnerSettings, Partner}
import com.echoed.chamber.domain.Topic


trait PartnerEvent extends Event

import com.echoed.chamber.services.partner.{PartnerEvent => PE}

private[services] case class PartnerCreated(
        partner: Partner,
        partnerSettings: PartnerSettings,
        partnerUser: PartnerUser) extends PE with CreatedEvent

private[services] trait TopicEvent { def topic: Topic }
private[services] case class TopicCreated(topic: Topic) extends PE with CreatedEvent with TopicEvent
private[services] case class TopicUpdated(topic: Topic) extends PE with UpdatedEvent with TopicEvent
