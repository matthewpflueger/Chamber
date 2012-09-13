package com.echoed.chamber.services.state.schema

import com.echoed.chamber.domain
import org.squeryl.KeyedEntity
import java.util.Date

case class Image (
        id: String,
        updatedOn: Long,
        createdOn: Long,
        url: String,
        originalUrl: String,
        originalWidth: Int,
        originalHeight: Int,
        sizedUrl: String,
        sizedWidth: Int,
        sizedHeight: Int,
        exhibitUrl: String,
        exhibitWidth: Int,
        exhibitHeight: Int,
        storyUrl: String,
        storyWidth: Int,
        storyHeight: Int,
        thumbnailUrl: String,
        thumbnailWidth: Int,
        thumbnailHeight: Int,
        processedOn: Long,
        processedStatus: String,
        retries: Int) extends KeyedEntity[String] {

    def this() = this(
        "",
        0L,
        0L,
        "",
        "",
        0,
        0,
        "",
        0,
        0,
        "",
        0,
        0,
        "",
        0,
        0,
        "",
        0,
        0,
        0L,
        "",
        0)

    def convertTo = domain.Image(
            id = id,
            updatedOn = updatedOn,
            createdOn = createdOn,
            url = url,
            originalUrl = originalUrl,
            originalWidth = originalWidth,
            originalHeight = originalHeight,
            sizedUrl = sizedUrl,
            sizedWidth = sizedWidth,
            sizedHeight = sizedHeight,
            exhibitUrl = exhibitUrl,
            exhibitWidth = exhibitWidth,
            exhibitHeight = exhibitHeight,
            storyUrl = storyUrl,
            storyWidth = storyWidth,
            storyHeight = storyHeight,
            thumbnailUrl = thumbnailUrl,
            thumbnailWidth = thumbnailWidth,
            thumbnailHeight = thumbnailHeight,
            processedOn = processedOn,
            processedStatus = processedStatus,
            retries = retries)
}


