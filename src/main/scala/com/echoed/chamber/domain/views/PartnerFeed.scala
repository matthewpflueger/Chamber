package com.echoed.chamber.domain.views
import java.util.{ArrayList, List => JList}

import com.echoed.chamber.domain.public.PartnerPublic

case class PartnerFeed(
    partner: PartnerPublic,
    echoes: JList[EchoViewPublic]) {

    def this(partner: PartnerPublic)= this(partner, new ArrayList[EchoViewPublic])

}
