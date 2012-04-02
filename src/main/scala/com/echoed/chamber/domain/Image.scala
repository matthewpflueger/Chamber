package com.echoed.chamber.domain

import java.util.{UUID, Date}


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
        thumbnailUrl: String,
        thumbnailWidth: Int,
        thumbnailHeight: Int,
        processedOn: Date,
        processedStatus: String) {


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
        null)

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

    val isValidFormat = Image.imageFormats.contains(ext)
    val originalFileName = findFileName(originalUrl)
    val sizedFileName = findFileName(sizedUrl)
    val thumbnailFileName = findFileName(thumbnailUrl)

    val hasOriginal = originalUrl != null && originalWidth > 0 && originalHeight > 0
    val hasSized = sizedUrl != null && sizedWidth > 0 && sizedHeight > 0
    val hasThumbnail = thumbnailUrl != null && thumbnailWidth > 0 && thumbnailHeight > 0

    val isProcessed = processedOn != null && hasOriginal && hasSized && hasThumbnail
    
    val preferredUrl = if (hasSized) sizedUrl else if (hasOriginal) originalUrl else url
    val preferredWidth = if (hasSized) sizedWidth else if (hasOriginal) originalWidth else 0
    val preferredHeight = if (hasSized) sizedHeight else if (hasOriginal) originalHeight else 0

}


object Image {
    val imageFormats = List("jpeg", "jpg", "png", "gif")
}