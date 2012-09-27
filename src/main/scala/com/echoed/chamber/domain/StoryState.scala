package com.echoed.chamber.domain

import com.echoed.chamber.domain.partner.{PartnerSettings, Partner}
import com.echoed.chamber.domain.views.StoryFull
import scala.collection.JavaConversions._
import com.echoed.util.UUID
import com.echoed.util.DateUtils._
import java.util.Date
import collection.immutable.HashMap

case class StoryState(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        title: String,
        productInfo: String,
        views: Int,
        tag: String,
        echoedUser: EchoedUser,
        imageId: String,
        image: Option[Image],
        chapters: List[Chapter],
        chapterImages: List[ChapterImage],
        comments: List[Comment],
        partner: Partner,
        partnerSettings: PartnerSettings,
        echo: Option[Echo],
        moderations: List[Moderation],
        votes: HashMap[String, Vote]) extends DomainObject {

    def this(
            eu: EchoedUser,
            p: Partner,
            ps: PartnerSettings,
            e: Option[Echo] = None,
            img: Option[Image] = None) = this(
        null,
        0L,
        0L,
        null,
        null,
        0,
        null,
        eu,
        img.map(_.id).orNull,
        img,
        List.empty[Chapter],
        List.empty[ChapterImage],
        List.empty[Comment],
        p,
        ps,
        e,
        List.empty[Moderation],
        HashMap.empty[String, Vote])

    def isCreated = id != null && createdOn > 0
    def create(title: String, productInfo: String, imageId: String) = {
        val storyState = copy(
            id = UUID(),
            updatedOn = new Date,
            createdOn = new Date,
            title = title,
            productInfo = productInfo,
            imageId = imageId)
        if (partnerSettings.moderateAll) storyState.moderate(partner.name, "Partner", partner.id)
        else storyState
    }

    def asStory = Story(
            id,
            updatedOn,
            createdOn,
            echoedUser.id,
            partner.id,
            partner.handle,
            partnerSettings.id,
            image.map(_.id).orNull,
            image.orNull,
            title,
            echo.map(_.id).orNull,
            echo.map(_.productId).orNull,
            productInfo,
            views,
            comments.size,
            tag)

    def asStoryInfo = StoryInfo(echoedUser, echo.orNull, partner, partnerSettings.makeStoryPrompts, asStoryFull.orNull)
    def asStoryFull =
            if (!isCreated) None
            else Option(StoryFull(id, asStory, echoedUser, chapters, chapterImages, comments, votes, moderationDescription))

    private def echoedModeratePredicate: Moderation => Boolean = _.moderatedRef == "AdminUser"
    private def moderatedPredicate: Moderation => Boolean = _.moderatedRef != "AdminUser"

    val isEchoedModerated = moderated(echoedModeratePredicate)
    val isModerated = moderated(moderatedPredicate)

    def moderate(
            moderatedBy: String,
            moderatedRef: String,
            moderatedRefId: String,
            moderated: Boolean = true) = {
        val moderation = new Moderation(
                "Story",
                this.id,
                moderatedBy,
                moderatedRef,
                moderatedRefId,
                moderated)
        copy(moderations = moderation :: moderations)
    }

    def moderationDescription = {
        val mod = findModeration(moderatedPredicate)
        val echoedMod = findModeration(echoedModeratePredicate)
        if (mod.isEmpty && echoedMod.isEmpty) None else Option(new ModerationDescription(mod, echoedMod))
    }

    private def moderated(predicate: Moderation => Boolean) =
            findModeration(predicate).map(_.moderated).getOrElse(false)

    private def findModeration(predicate: Moderation => Boolean) =
            moderations.sortWith(_.createdOn > _.createdOn).filter(predicate).headOption

}