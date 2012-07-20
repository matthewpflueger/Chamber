package com.echoed.chamber.services.tag
import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
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
        value: Either[TE, JList[Tag]]) extends TM with RM[JList[Tag], GetTags, TE]

case class AddTag(tagId: String) extends TM
case class AddTagResponse(
        message: AddTag,
        value: Either[TE, Tag]) extends TM with RM[Tag, AddTag, TE]

case class DecreaseTagCount(tagId: String) extends TM
case class DecreaseTagCountResponse(
        message: DecreaseTagCount,
        value: Either[TE, Int]) extends TM with RM[Int, DecreaseTagCount, TE]

case class WriteTag(tagId: String) extends TM
case class WriteTagResponse(
        message: WriteTag,
        value: Either[TE, Tag]) extends TM with RM[Tag, WriteTag, TE]