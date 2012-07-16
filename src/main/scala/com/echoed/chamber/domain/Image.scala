package com.echoed.chamber.domain

import java.util.{UUID, Date}
import java.net.URLEncoder


case class Image(
        id: String,
        updatedOn: Date,
        createdOn: Date,
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
        processedOn: Date,
        processedStatus: String,
        retries: Int) {


    def this(url: String) = this(
        UUID.randomUUID().toString,
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
        null,
        null,
        0)

    require(url != null)


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

    val isProcessed = processedOn != null && hasOriginal && hasSized && hasThumbnail && hasExhibit
    
    val preferredUrl = if (hasExhibit) exhibitUrl else if (hasSized) sizedUrl else if (hasStory) storyUrl else if (hasOriginal) originalUrl else url
    val preferredWidth = if (hasExhibit) exhibitWidth else if (hasSized) sizedWidth else if (hasStory) storyWidth else if (hasOriginal) originalWidth else 0
    val preferredHeight = if (hasExhibit) exhibitHeight else if (hasSized) sizedHeight else if (hasStory) storyHeight else if (hasOriginal) originalHeight else 0

}


object Image {
    val imageFormats = List("jpeg", "jpg", "png", "gif")
}