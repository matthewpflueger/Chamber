package com.echoed.chamber.services.feed

import com.echoed.chamber.services.{MessageResponse => MR, Event, EchoedException, Message}
import com.echoed.chamber.domain.views._
import com.echoed.chamber.domain.public.StoryPublic
import com.echoed.chamber.domain.{Topic, Community}
import context.PublicContext

sealed trait FeedMessage extends Message

sealed case class FeedException(
        message: String = "",
        cause: Throwable = null,
        code: Option[String] = None,
        arguments: Option[Array[AnyRef]] = None) extends EchoedException(message, cause, code, arguments)

import com.echoed.chamber.services.feed.{FeedMessage => FM}
import com.echoed.chamber.services.feed.{FeedException => FE}

case class GetPartnerIds() extends FM
case class GetPartnerIdsResponse(
        message: GetPartnerIds,
        value: Either[FE, List[String]]) extends FM with MR[List[String], GetPartnerIds, FE]

case class GetStoryIds() extends FM
case class GetStoryIdsResponse(message: GetStoryIds, value: Either[FE, Array[String]])
        extends FM with MR[Array[String], GetStoryIds, FE]

case class GetStory(storyId: String, origin: String) extends FM
case class GetStoryResponse(message: GetStory, value: Either[FE, Option[StoryPublic]])
        extends FM with MR[Option[StoryPublic], GetStory, FE]

private[services] case class RequestTopicStoryFeed(topicId: String, page: Int) extends FM
private[services] case class RequestTopicStoryFeedResponse(
            message: RequestTopicStoryFeed,
            value: Either[FE, Feed[PublicContext]]) extends FM with MR[Feed[PublicContext], RequestTopicStoryFeed, FE]

case class RequestPublicContent(page: Int) extends FM
case class RequestPublicContentResponse(
            message: RequestPublicContent,
            value: Either[FE, Feed[PublicContext]]) extends FM with MR[Feed[PublicContext], RequestPublicContent, FE]
