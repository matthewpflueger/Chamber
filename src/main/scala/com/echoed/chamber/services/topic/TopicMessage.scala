package com.echoed.chamber.services.topic
import com.echoed.chamber.services.{MessageResponse => MR, Event, EchoedException, Message}
import com.echoed.chamber.domain.Topic
import com.echoed.chamber.domain.views.TopicStoryFeed

sealed trait TopicMessage extends Message
sealed case class TopicException(
    message: String = "",
    cause: Throwable = null,
    code: Option[String] = None,
    arguments: Option[Array[AnyRef]] = None) extends EchoedException(message, cause, code, arguments)

import com.echoed.chamber.services.topic.{TopicMessage => TM}
import com.echoed.chamber.services.topic.{TopicException => TE}


case class GetTopics() extends TM
case class GetTopicsResponse(
            message: GetTopics,
            value: Either[TE, List[Topic]]) extends TM with MR [List[Topic], GetTopics, TE]

case class ReadTopicFeed(topicId: String, page: Int) extends TM
case class ReadTopicFeedResponse(
            message: ReadTopicFeed,
            value: Either[TE, TopicStoryFeed]) extends TM with MR [TopicStoryFeed, ReadTopicFeed, TE]

case class ReadCommunityTopics(communityId: String) extends TM
case class ReadCommunityTopicsResponse(
            message: ReadCommunityTopics,
            value: Either[TE, List[Topic]]) extends TM with MR [List[Topic], ReadCommunityTopics, TE]

private[services] case class RequestPartnerTopics(partnerId: String) extends TM
private[services] case class RequestPartnerTopicsResponse(
            message: RequestPartnerTopics,
            value: Either[TE, List[Topic]]) extends TM with MR [List[Topic], RequestPartnerTopics, TE]

