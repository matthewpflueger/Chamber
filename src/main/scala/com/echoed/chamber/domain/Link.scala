package com.echoed.chamber.domain

import java.util.Date
import com.echoed.util.UUID
import com.echoed.util.DateUtils._
import org.squeryl.annotations.Transient


case class Link(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        echoedUserId: String,
        partnerId: String,
        partnerHandle: String,
        partnerSettingsId: String,
        storyId: String,
        chapterId: String,
        url: String,
        description: Option[String],
        pageTitle: Option[String],
        imageId: Option[String],
        image: Image) extends DomainObject {

    @Transient
    val title = description.getOrElse(pageTitle.getOrElse(url))
}

object Link {

    def apply() = new Link("", 0L, 0L, "", "", "", "", "", "", "", Some(""), Some(""), Some(""), Image())

    def apply(story: Story, url: String) =  new Link(
        UUID(),
        new Date,
        new Date,
        story.echoedUserId,
        story.partnerId,
        story.partnerHandle,
        story.partnerSettingsId,
        story.id,
        null,
        url,
        None,
        None,
        None,
        null)

    def apply(chapter: Chapter, link: Link) = new Link(
        link.id,
        link.updatedOn,
        link.createdOn,
        chapter.echoedUserId,
        chapter.partnerId,
        chapter.partnerHandle,
        chapter.partnerSettingsId,
        chapter.storyId,
        chapter.id,
        link.url,
        link.description,
        link.pageTitle,
        link.imageId,
        link.image)
}
