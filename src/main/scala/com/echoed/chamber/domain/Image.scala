package com.echoed.chamber.domain

import java.util.Date
import java.net.URL
import com.google.common.io.ByteStreams


case class Image(
        imageUrl: String,
        imageOriginalUrl: String,
        imageWidth: Int,
        imageHeight: Int,
        imageGrabbedOn: Date,
        imageGrabbedStatus: String,
        imageValidatedOn: Date) {

    def this(imageUrl: String) = this(imageUrl, imageUrl, 0, 0, null, null, null)

    val isProcessed = imageGrabbedOn != null || imageGrabbedStatus != null
    val isGrabbedFailed = imageGrabbedOn == null && imageGrabbedStatus != null
    val isGrabbed = imageGrabbedOn != null
}


