package com.echoed.chamber.domain.views.content

trait Content extends FeedItem {

    def updatedOn:     Long
    def createdOn:     Long

    def numViews:      Int
    def numVotes:      Int
    def numComments:   Int

    def contentDescription:     ContentDescription

}

case class ContentDescription(
    singular:      String,
    plural:        String,
    endPoint:      String
)