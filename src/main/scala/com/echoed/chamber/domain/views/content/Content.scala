package com.echoed.chamber.domain.views.content

trait Content extends FeedItem{

    def updatedOn:     Long
    def createdOn:     Long

    def numViews:         Int
    def numVotes:         Int
    def numComments:      Int

    def plural:        String
    def singular:      String
    def endPoint:      String

}
