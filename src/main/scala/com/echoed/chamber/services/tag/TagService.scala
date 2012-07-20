package com.echoed.chamber.services.tag
import akka.dispatch.Future

trait TagService {

    def getTags(filter: String): Future[GetTagsResponse]

    def addTag(tagId: String): Future[AddTagResponse]

}
