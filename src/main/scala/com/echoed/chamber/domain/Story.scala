package com.echoed.chamber.domain

import java.util.Date
import partner.{PartnerSettings, Partner}
import com.echoed.util.UUID
import com.echoed.util.DateUtils._


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
        title: String,
        echoId: String,
        productId: String,
        productInfo: String,
        views: Int,
        comments: Int,
        tag: String) extends DomainObject {

    def this() = this(
        id = "",
        updatedOn = 0L,
        createdOn = 0L,
        echoedUserId = "",
        partnerId = "",
        partnerHandle = "",
        partnerSettingsId = "",
        imageId = "",
        image = new Image(),
        title = "",
        echoId = "",
        productId = "",
        productInfo = "",
        views = 0,
        comments = 0,
        tag = "")

    def this(
            echoedUser: EchoedUser,
            partner: Partner,
            partnerSettings: PartnerSettings,
            _image: Image,
            _title: String,
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
        tag = null)

}

object Story {
    def apply(s: StoryState): Story = Story(
        id = s.id,
        updatedOn = s.updatedOn,
        createdOn = s.createdOn,
        echoedUserId = s.echoedUser.id,
        partnerId = s.partner.id,
        partnerHandle = s.partner.handle,
        partnerSettingsId = s.partnerSettings.id,
        imageId = s.image.id,
        image = s.image,
        title = s.title,
        echoId = s.echo.map(_.id).orNull,
        productId = s.echo.map(_.productId).orNull,
        productInfo = s.productInfo,
        views = s.views,
        comments = s.comments.size,
        tag = s.tag)
}