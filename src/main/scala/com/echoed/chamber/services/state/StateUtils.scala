package com.echoed.chamber.services.state

import com.echoed.chamber.domain.{StoryState, Story}
import org.squeryl.PrimitiveTypeMode._
import com.echoed.chamber.services.state.schema.ChamberSchema._

private[state] object StateUtils {

    def readStory(s: Story, echo: Option[schema.Echo] = None) = {
        val c = from(chapters)(c => where(c.storyId === s.id) select(c)).toList
        val ci = from(chapterImages)(ci => where(ci.storyId === s.id) select(ci)).map { ci =>
            images.lookup(ci.imageId).map(img => ci.copy(image = img.convertTo)).get
        }.toList

        val cm = from(comments)(cm => where(cm.storyId === s.id) select(cm)).map { cm =>
            echoedUsers.lookup(cm.byEchoedUserId).map(eu => cm.copy(echoedUser = eu)).get
        }.toList

        val eu = echoedUsers.lookup(s.echoedUserId).get
        val img = images.lookup(s.imageId).get.convertTo
        val p = partners.lookup(s.partnerId).get
        val ps = partnerSettings.lookup(s.partnerSettingsId).get
        val e = echo.orElse(Option(s.echoId).flatMap(echoes.lookup(_)))
        val m = from(moderations)(m => where(m.refId === s.id) select(m)).toList

        StoryState(
                s.id,
                s.updatedOn,
                s.createdOn,
                s.title,
                s.productInfo,
                s.views,
                s.tag,
                eu,
                img,
                c,
                ci,
                cm,
                p,
                ps,
                e.map(_.convertTo(img)),
                m)
    }
}

