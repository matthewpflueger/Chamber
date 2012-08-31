package com.echoed.chamber.domain.views

import com.echoed.chamber.domain._
import partner.{PartnerSettings, Partner}


case class EchoPossibilityView(
        echoes: List[Echo],
        partner: Partner,
        partnerSettings: PartnerSettings) {

    def this(echo: Echo, partner: Partner, partnerSettings: PartnerSettings) = this(
        List(echo),
        partner,
        partnerSettings)

    val echo = echoes.head
}

