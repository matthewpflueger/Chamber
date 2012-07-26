package com.echoed.chamber.services.tag
import com.echoed.chamber.services.{MessageResponse => MR, Event, EchoedException, Message}
import java.util.{List => JList}
import com.echoed.chamber.domain.Tag

sealed trait TagMessage extends Message

sealed case class TagException(
        message: String = "",
        cause: Throwable = null,
        code: Option[String] = None,
        arguments: Option[Array[AnyRef]] = None) extends EchoedException(message, cause, code, arguments)

import com.echoed.chamber.services.tag.{ TagMessage => TM, TagException => TE }

case class GetTags(filter: String) extends TM
case class GetTagsResponse(
        message: GetTags,
        value: Either[TE, JList[Tag]]) extends TM with MR[JList[Tag], GetTags, TE]

case class GetTopTags() extends TM
case class GetTopTagsResponse(
        message: GetTopTags,
        value: Either[TE, JList[Tag]]) extends TM with MR[JList[Tag], GetTopTags, TE]

case class AddTag(tagId: String) extends TM
case class AddTagResponse(
        message: AddTag,
        value: Either[TE, Tag]) extends TM with MR[Tag, AddTag, TE]

case class ApproveTag(tagId: String) extends TM
case class ApproveTagResponse(
        message: ApproveTag,
        value: Either[TE, Tag]) extends TM with MR[Tag, ApproveTag, TE]

case class RemoveTag(tagId: String) extends TM
case class RemoveTagResponse(
        message: RemoveTag,
        value: Either[TE, Tag]) extends TM with MR[Tag, RemoveTag, TE]

case class ReplaceTag(ogTagId: String, newTagId: String) extends TM
case class ReplaceTagResponse(
        message: ReplaceTag,
        value: Either[TE, Tag]) extends TM with MR[Tag, ReplaceTag, TE]

case class WriteTag(tagId: String) extends TM
case class WriteTagResponse(
        message: WriteTag,
        value: Either[TE, Tag]) extends TM with MR[Tag, WriteTag, TE]

case class TagAdded(tagId: String) extends TM with Event
case class TagReplaced(originalTagId: String, tagId: String) extends TM with Event
