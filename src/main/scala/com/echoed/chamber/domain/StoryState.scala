package com.echoed.chamber.domain

import com.echoed.chamber.domain.partner.{PartnerSettings, Partner}
import com.echoed.chamber.domain.views.StoryFull
import scala.collection.JavaConversions._
import com.echoed.util.UUID
import com.echoed.util.DateUtils._
import java.util.Date

case class StoryState(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        title: String,
        productInfo: String,
        views: Int,
        tag: String,
        echoedUser: EchoedUser,
        image: Image,
        chapters: List[Chapter],
        chapterImages: List[ChapterImage],
        comments: List[Comment],
        partner: Partner,
        partnerSettings: PartnerSettings,
        echo: Option[Echo]) extends DomainObject {

    def this(eu: EchoedUser, p: Partner, ps: PartnerSettings, e: Option[Echo] = None, img: Option[Image] = None) = this(
            null,
            0L,
            0L,
            null,
            null,
            0,
            null,
            eu,
            img.orNull,
            List.empty[Chapter],
            List.empty[ChapterImage],
            List.empty[Comment],
            p,
            ps,
            e)

    def isCreated = id != null && createdOn > 0
    def create(title: String, productInfo: String, image: Image) = copy(
            id = UUID(),
            updatedOn = new Date,
            createdOn = new Date,
            title = title,
            productInfo = productInfo,
            image = image)

    def asStory = Story(
            id,
            updatedOn,
            createdOn,
            echoedUser.id,
            partner.id,
            partner.handle,
            partnerSettings.id,
            image.id,
            image,
            title,
            echo.map(_.id).orNull,
            echo.map(_.productId).orNull,
            productInfo,
            views,
            comments.size,
            tag)

    def asStoryInfo = StoryInfo(echoedUser, echo.orNull, partner, partnerSettings.makeStoryPrompts, asStoryFull.orNull)
    def asStoryFull = if (!isCreated) None else Option(StoryFull(id, asStory, echoedUser, chapters, chapterImages, comments))
}

