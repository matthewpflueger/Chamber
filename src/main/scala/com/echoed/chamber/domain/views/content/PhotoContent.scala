package com.echoed.chamber.domain.views.content

import com.echoed.chamber.domain.Image

case class PhotoContent( image: Image, originalContent: Content )  extends Content {

    val _type = "photo"
    def _title = null

    def _id = image.id
    def _createdOn = image.createdOn
    def _updatedOn = image.updatedOn
    def _views = 0
    def _votes = 0
    def _comments = 0

    def _plural = "Photos"
    def _singular = "Photo"
    def _endPoint = "photos"

}
