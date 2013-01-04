package com.echoed.chamber.domain

import java.util.Date
import java.net.URLEncoder
import com.echoed.util.UUID
import com.echoed.util.DateUtils._


case class Image(
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
        retries: Int) extends DomainObject {

    def this() = this(
        "",
        new Date,
        new Date,
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
        new Date,
        "",
        0)

    def this(url: String) = this(
        UUID(),
        new Date,
        new Date,
        url,
        null,
        0,
        0,
        null,
        0,
        0,
        null,
        0,
        0,
        null,
        0,
        0,
        null,
        0,
        0,
        0L,
        null,
        0)

    def this(id: String, url: String, width: Int, height: Int, cloudName: String) = this(
        id,
        new Date,
        new Date,
        url,
        url,
        width,
        height,
        url,
        width,
        height,
        url,
        width,
        height,
        url,
        width,
        height,
        url,
        width,
        height,
        new Date,
        cloudName,
        0)


    private def findFileName(url: String) =
        Option(url).map { u =>
            var i = u.lastIndexOf('/')
            var f = if (i > -1) u.substring(i + 1) else u
            i = f.lastIndexOf('?')
            if (i > -1) f.substring(0, i) else f
        }


    val fileName = findFileName(url).get
    val ext = {
        val i = fileName.lastIndexOf('.')
        (if (i > -1) fileName.substring(i + 1) else "").toLowerCase
    }

    val urlWithEncodedFileName = url.replace(fileName, URLEncoder.encode(fileName, "UTF-8").replace("+", "%20"))

    val isValidFormat = Image.imageFormats.contains(ext)
    val originalFileName = findFileName(originalUrl)
    val sizedFileName = findFileName(sizedUrl)
    val thumbnailFileName = findFileName(thumbnailUrl)
    val exhibitFileName = findFileName(exhibitUrl)
    val storyFileName = findFileName(storyUrl)

    val hasOriginal = originalUrl != null && originalWidth > 0 && originalHeight > 0
    val hasSized = sizedUrl != null && sizedWidth > 0 && sizedHeight > 0
    val hasThumbnail = thumbnailUrl != null && thumbnailWidth > 0 && thumbnailHeight > 0
    val hasExhibit = exhibitUrl != null && exhibitWidth > 0 && exhibitHeight > 0
    val hasStory = storyUrl != null && storyWidth > 0 && storyHeight > 0

    val isProcessed = processedOn > 0 && hasOriginal && hasStory && hasExhibit && hasSized && hasThumbnail
    
    val preferredUrl = {
        var pUrl = if (hasExhibit) exhibitUrl else if (hasSized) sizedUrl else if (hasStory) storyUrl else if (hasOriginal) originalUrl else url
        if(isCloudinary) pUrl += ".jpg"
        pUrl
    }
    val preferredWidth = if (hasExhibit) exhibitWidth else if (hasSized) sizedWidth else if (hasStory) storyWidth else if (hasOriginal) originalWidth else 0
    val preferredHeight = if (hasExhibit) exhibitHeight else if (hasSized) sizedHeight else if (hasStory) storyHeight else if (hasOriginal) originalHeight else 0

    val isCloudinary = processedStatus != null && processedStatus.startsWith("echoed")
    val cloudName = if (isCloudinary) processedStatus else null
}


object Image {
    val imageFormats = List("jpeg", "jpg", "png", "gif")
}

