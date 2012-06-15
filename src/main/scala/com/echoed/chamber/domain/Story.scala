package com.echoed.chamber.domain

import java.util.{UUID, Date}
import com.echoed.util.DateUtils._
import partner.{PartnerSettings, Partner}


case class Story(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        echoedUserId: String,
        partnerId: String,
        partnerSettingsId: String,
        imageId: String,
        title: String,
        echoId: String,
        productId: String) {

    def this(
            echoedUser: EchoedUser,
            partner: Partner,
            partnerSettings: PartnerSettings,
            image: Image,
            title: String,
            echo: Option[Echo] = None,
            productId: Option[String] = None) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        echoedUser.id,
        partner.id,
        partnerSettings.id,
        image.id,
        title,
        echo.map(_.id).orNull,
        echo.map(_.productId).orElse(productId).orNull)
}
