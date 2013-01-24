package com.echoed.chamber.domain.views.content

import com.echoed.chamber.domain.Image

case class PhotoContent( image: Image, originalContent: Content )  extends Content with FeedItem{

    val contentType = "Photo"
    val id = image.id

    def title = null
    def createdOn = image.createdOn
    def updatedOn = image.updatedOn
    def numViews = 0
    def numVotes = 0
    def numComments = 0

    def plural = "Photos"
    def singular = "Photo"
    def endPoint = "photos"

}
