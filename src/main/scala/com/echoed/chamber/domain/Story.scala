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
        image: Image,
        title: String,
        echoId: String,
        productId: String,
        productInfo: String) {

    def this(
            echoedUser: EchoedUser,
            partner: Partner,
            partnerSettings: PartnerSettings,
            image: Image,
            title: String,
            echo: Option[Echo] = None,
            productInfo: Option[String] = None) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        echoedUser.id,
        partner.id,
        partnerSettings.id,
        image,
        title,
        echo.map(_.id).orNull,
        echo.map(_.productId).orNull,
        productInfo.orNull)

}
