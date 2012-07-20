package com.echoed.chamber.domain

import com.echoed.util.UUID
import java.util.Date
import com.echoed.util.DateUtils._


case class Chapter(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        storyId: String,
        echoedUserId: String,
        partnerId: String,
        partnerHandle: String,
        partnerSettingsId: String,
        echoId: String,
        title: String,
        text: String,
        publishedOn: Long) extends DomainObject {

    def this(
            story: Story,
            _title: String,
            _text: String) = this(
        id = UUID(),
        updatedOn = new Date,
        createdOn = new Date,
        storyId = story.id,
        echoedUserId = story.echoedUserId,
        partnerHandle = story.partnerHandle,
        partnerId = story.partnerId,
        partnerSettingsId = story.partnerSettingsId,
        echoId = story.echoId,
        title = _title,
        text = _text,
        publishedOn = 0)

}


