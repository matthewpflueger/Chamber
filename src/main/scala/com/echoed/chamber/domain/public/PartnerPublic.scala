package com.echoed.chamber.domain.public

import com.echoed.chamber.domain.partner.Partner

case class PartnerPublic(
        id: String,
        name: String,
        handle: String,
        domain: String,
        logo: String) {

    def this(partner: Partner) =
        this(
            partner.id,
            partner.name,
            partner.handle,
            partner.domain,
            partner.logo)

}
