package com.echoed.chamber.domain

import java.util.{UUID, Date}
import com.echoed.util.DateUtils._


case class ChapterImage(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        storyId: String,
        echoedUserId: String,
        partnerId: String,
        partnerSettingsId: String,
        echoId: String,
        chapterId: String,
        image: Image) {

    def this(
            chapter: Chapter,
            _image: Image) = this(
        id = UUID.randomUUID.toString,
        updatedOn = new Date,
        createdOn = new Date,
        storyId = chapter.storyId,
        echoedUserId = chapter.echoedUserId,
        partnerId = chapter.partnerId,
        partnerSettingsId = chapter.partnerSettingsId,
        echoId = chapter.echoId,
        chapterId = chapter.id,
        image = _image)

}
