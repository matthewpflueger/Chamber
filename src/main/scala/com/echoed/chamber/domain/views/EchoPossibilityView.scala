package com.echoed.chamber.domain.views

import com.echoed.chamber.domain._
import partner.{PartnerSettings, Partner}


case class EchoPossibilityView(
        echoPossibilities: List[Echo],
        partner: Partner,
        partnerSettings: PartnerSettings,
        echoedUser: EchoedUser,
        echo: Echo) {

    def this(echoPossibilities: List[Echo], partner: Partner, partnerSettings: PartnerSettings) = this(
        echoPossibilities,
        partner,
        partnerSettings,
        null,
        null)

    def this(echoPossibility: Echo, partner: Partner, partnerSettings: PartnerSettings) = this(
        List(echoPossibility),
        partner,
        partnerSettings)

    val echoPossibility = echoPossibilities(0)
}

