package com.echoed.chamber.domain.views
import java.util.{ArrayList, List => JList}

import com.echoed.chamber.domain.public.PartnerPublic
import java.util

case class PartnerFeed(
        partner: PartnerPublic,
        echoes: JList[EchoViewPublic],
        stories: JList[StoryFull]) {

    def this(partner: PartnerPublic)= this(partner, new ArrayList[EchoViewPublic], new util.ArrayList[StoryFull])

    def this(partner: PartnerPublic, echoes: JList[EchoViewPublic]) = this(partner, echoes, new ArrayList[StoryFull])

}
