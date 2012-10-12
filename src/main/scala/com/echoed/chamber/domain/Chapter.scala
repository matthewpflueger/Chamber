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

    def this() = this(
        id = "",
        updatedOn = 0L,
        createdOn = 0L,
        storyId = "",
        echoedUserId = "",
        partnerHandle = "",
        partnerId = "",
        partnerSettingsId = "",
        echoId = "",
        title = "",
        text = "",
        publishedOn = 0)

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

    val isPublished = publishedOn > 0

}


