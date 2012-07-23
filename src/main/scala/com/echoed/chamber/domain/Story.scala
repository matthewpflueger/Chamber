package com.echoed.chamber.domain

import java.util.Date
import com.echoed.util.DateUtils._
import partner.{PartnerSettings, Partner}
import com.echoed.util.UUID


case class Story(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        echoedUserId: String,
        partnerId: String,
        partnerHandle: String,
        partnerSettingsId: String,
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
        image = _image,
        title = _title,
        echoId = echo.map(_.id).orNull,
        productId = echo.map(_.productId).orNull,
        productInfo = _productInfo.orNull,
        views = 0,
        comments = 0,
        tag = null)

}
