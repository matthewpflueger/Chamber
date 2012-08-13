package com.echoed.chamber.domain

import java.util.Date
import com.echoed.util.DateUtils._
import com.echoed.util.UUID


case class ChapterImage(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        storyId: String,
        echoedUserId: String,
        partnerId: String,
        partnerHandle: String,
        partnerSettingsId: String,
        echoId: String,
        chapterId: String,
        image: Image) extends DomainObject {

    def this() = this(
        id = "",
        updatedOn = 0L,
        createdOn = 0L,
        storyId = "",
        echoedUserId = "",
        partnerId = "",
        partnerHandle = "",
        partnerSettingsId = "",
        echoId = "",
        chapterId = "",
        image = new Image())

    def this(
            chapter: Chapter,
            _image: Image) = this(
        id = UUID(),
        updatedOn = new Date,
        createdOn = new Date,
        storyId = chapter.storyId,
        echoedUserId = chapter.echoedUserId,
        partnerId = chapter.partnerId,
        partnerHandle = chapter.partnerHandle,
        partnerSettingsId = chapter.partnerSettingsId,
        echoId = chapter.echoId,
        chapterId = chapter.id,
        image = _image)

}
