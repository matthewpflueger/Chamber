package com.echoed.chamber.domain

import com.echoed.chamber.domain.partner.{PartnerSettings, Partner}
import views.{StoryCommunities, StoryFull}
import scala.collection.JavaConversions._
import com.echoed.util.UUID
import com.echoed.util.DateUtils._
import java.util.Date
import com.echoed.chamber.domain.public.StoryPublic

case class StoryState(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        title: Option[String],
        views: Int,
        community: String,
        echoedUser: EchoedUser,
        imageId: String,
        image: Option[Image],
        chapters: List[Chapter],
        chapterImages: List[ChapterImage],
        comments: List[Comment],
        links: List[Link],
        partner: Partner,
        partnerSettings: PartnerSettings,
        echo: Option[Echo],
        moderations: List[Moderation],
        votes: Map[String, Vote],
        topic: Option[Topic],
        contentType: String,
        contentPath: Option[String],
        contentPageTitle: Option[String]) extends DomainObject {

    def this(
            eu: EchoedUser,
            p: Partner,
            ps: PartnerSettings,
            e: Option[Echo] = None,
            img: Option[Image] = None,
            topic: Option[Topic] = None,
            community: Option[String] = None,
            contentType: String = "Story",
            contentPath: Option[String] = None,
            contentPageTitle: Option[String] = None) = this(
        UUID(),
        0L,
        0L,
        None,
        0,
        community.getOrElse(p.category),
        eu,
        img.map(_.id).orNull,
        img,
        List.empty[Chapter],
        List.empty[ChapterImage],
        List.empty[Comment],
        List.empty[Link],
        p,
        ps,
        e,
        List.empty[Moderation],
        Map.empty[String, Vote],
        topic,
        contentType,
        contentPath,
        contentPageTitle)

    def isCreated = createdOn > 0
    def create(
            title: Option[String],
            productInfo: String,
            community: String,
            image: Option[Image],
            contentType: Option[String] = None,
            contentPath: Option[String] = None,
            contentPageTitle: Option[String] = None) = {
        val storyState = trySetContentTypeAndPath(contentType, contentPath, contentPageTitle).copy(
            updatedOn = new Date,
            createdOn = new Date,
            title = title,
            community = community,
            imageId = image.map(_.id).orNull,
            image = image)
        if (partnerSettings.moderateAll) storyState.moderate(partner.name, "Partner", partner.id)
        else storyState
    }

    def trySetContentTypeAndPath(ct: Option[String], cp: Option[String], cpt: Option[String]) = copy(
            contentType = ct.getOrElse(this.contentType),
            contentPath = cp.orElse(this.contentPath),
            contentPageTitle = cpt.orElse(this.contentPageTitle))

    def asStory = Story(
            id,
            updatedOn,
            createdOn,
            echoedUser.id,
            partner.id,
            partner.handle,
            partnerSettings.id,
            imageId,
            image.orNull,
            extractTitle,
            echo.map(_.id).orNull,
            echo.map(_.productId).orNull,
            null,
            views,
            numComments,
            upVotes,
            downVotes,
            community,
            topic.map(_.id).orNull,
            contentType,
            contentPath,
            contentPageTitle)

    def asStoryInfo = StoryInfo(
            echoedUser,
            echo.orNull,
            partner,
            partnerSettings.makeStoryPrompts,
            new StoryCommunities(),
            asStoryFull.orNull)

    def asStoryFull = Option(StoryFull(id, asStory, echoedUser, partner, chapters, chapterImages, comments, links, votes, moderationDescription, topic.orNull))

    def asStoryPublic = new StoryPublic(asStoryFull.get)

    def extractTitle = title.orElse {
        chapters.filter(_.isPublished).headOption.map(_.title).orElse(this.chapters.headOption.map(_.title))
    }

    private def selfModeratedPredicate: Moderation => Boolean = _.moderatedRef == "EchoedUser"
    private def echoedModeratedPredicate: Moderation => Boolean = _.moderatedRef == "AdminUser"
    private def partnerModeratedPredicate: Moderation => Boolean = _.moderatedRef == "PartnerUser"

    val isPublished: Boolean = chapters.foldLeft(false)((published, chapter) => published || chapter.isPublished)
    val isEchoedModerated = moderated(echoedModeratedPredicate)
    val isModerated = moderated(partnerModeratedPredicate)
    val isSelfModerated = moderated(selfModeratedPredicate)

    val numComments = comments.length
    val upVotes = votes.filter(t => t._2.value > 0).size
    val downVotes = votes.filter(t => t._2.value < 0).size

    def moderate(moderatedBy: String, moderatedRef: String, moderatedRefId: String, moderated: Boolean = true) =
        copy(moderations = new Moderation(
            "Story",
            this.id,
            moderatedBy,
            moderatedRef,
            moderatedRefId,
            moderated) :: moderations)


    val moderationDescription = new ModerationDescription(
        findModeration(partnerModeratedPredicate),
        findModeration(echoedModeratedPredicate),
        findModeration(selfModeratedPredicate))

    private def moderated(predicate: Moderation => Boolean) =
            findModeration(predicate).map(_.moderated).getOrElse(false)

    private def findModeration(predicate: Moderation => Boolean) =
            moderations.sortWith(_.createdOn > _.createdOn).filter(predicate).headOption

}