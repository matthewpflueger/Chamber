package com.echoed.chamber.domain.views
import java.util.{ArrayList, List => JList}

import com.echoed.chamber.domain.partner.Partner

case class PartnerFeed(
    partner: Partner,
    echoes: JList[EchoViewPublic]) {

    def this(partner: Partner)= this(partner, new ArrayList[EchoViewPublic])

}
