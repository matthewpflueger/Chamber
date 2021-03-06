package com.echoed.chamber.domain

import java.util.Date
import partner.{PartnerSettings, Partner}
import com.echoed.util.UUID
import com.echoed.util.DateUtils._
import com.echoed.chamber.domain.views.content.{Content, ContentDescription}


case class Story(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        echoedUserId: String,
        partnerId: String,
        partnerHandle: String,
        partnerSettingsId: String,
        imageId: String,
        image: Image,
        title: Option[String],
        echoId: String,
        productId: String,
        productInfo: String,
        views: Int,
        comments: Int,
        upVotes: Int,
        downVotes: Int,
        community: String,
        topicId: String,
        contentType: String,
        contentPath: Option[String],
        contentPageTitle: Option[String]) extends DomainObject {

    def this() = this(
            id = "",
            updatedOn = 0L,
            createdOn = 0L,
            echoedUserId = "",
            partnerId = "",
            partnerHandle = "",
            partnerSettingsId = "",
            imageId = "",
            image = Image(),
            title = Some(""),
            echoId = "",
            productId = "",
            productInfo = "",
            views = 0,
            comments = 0,
            upVotes = 0,
            downVotes = 0,
            community = "",
            topicId = "",
            contentType = "Story",
            contentPath = Some(""),
            contentPageTitle = Some(""))

    def this(
            echoedUser: EchoedUser,
            partner: Partner,
            partnerSettings: PartnerSettings,
            _image: Image,
            _title: Option[String] = None,
            echo: Option[Echo] = None,
            _productInfo: Option[String] = None) = this(
        id = UUID(),
        updatedOn = new Date,
        createdOn = new Date,
        echoedUserId = echoedUser.id,
        partnerId = partner.id,
        partnerHandle = partner.handle,
        partnerSettingsId = partnerSettings.id,
        imageId = _image.id,
        image = _image,
        title = _title,
        echoId = echo.map(_.id).orNull,
        productId = echo.map(_.productId).orNull,
        productInfo = _productInfo.orNull,
        views = 0,
        comments = 0,
        upVotes = 0,
        downVotes = 0,
        community = null,
        topicId = null,
        contentType = "Story",
        contentPath = None,
        contentPageTitle = None)

}
