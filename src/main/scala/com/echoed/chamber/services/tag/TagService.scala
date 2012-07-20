package com.echoed.chamber.services.tag
import akka.dispatch.Future

trait TagService {

    def getTags(filter: String): Future[GetTagsResponse]

    def addTag(tagId: String): Future[AddTagResponse]

    def removeTag(tagId: String): Future[RemoveTagResponse]

    def replaceTag(ogTagId: String, newTagId: String): Future[ReplaceTagResponse]

}
