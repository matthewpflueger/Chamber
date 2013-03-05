package com.echoed.chamber.domain.views.content

import com.echoed.chamber.domain.Image

case class PhotoContent( image: Image, originalContent: Content )  extends Content {

    val contentType = "Photo"
    val id = image.id

    def title = null
    def createdOn = image.createdOn
    def updatedOn = image.updatedOn
    def numViews = 0
    def numVotes = 0
    def numComments = 0

    def contentDescription = PhotoContent.contentDescription

}

object PhotoContent {

    val contentDescription = new ContentDescription("Photo", "Photos", "photos", 10)

}