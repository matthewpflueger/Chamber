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

case class GetCategoryStoryFeed(categoryId: String, page: Int) extends FM
case class GetCategoryStoryFeedResponse(
            message: GetCategoryStoryFeed,
            value: Either[FE, ContentFeed[PublicContext]]) extends FM with MR [ContentFeed[PublicContext], GetCategoryStoryFeed, FE]

case class GetUserPublicStoryFeed(echoedUserId: String) extends FM
case class GetUserPublicStoryFeedResponse(
            message: GetUserPublicStoryFeed,
            value: Either[FE, ContentFeed[PublicContext]]) extends FM with MR[ContentFeed[PublicContext], GetUserPublicStoryFeed, FE]

case class GetUserPrivateStoryFeed(echoedUserId: String, page: Int) extends FM
case class GetUserPrivateStoryFeedResponse(
            message: GetUserPrivateStoryFeed,
            value: Either[FE, ContentFeed[PublicContext]]) extends FM with MR[ContentFeed[PublicContext], GetUserPrivateStoryFeed, FE]

case class RequestPartnerStoryFeed(partnerId: String) extends FM
case class RequestPartnerStoryFeedResponse(
            message: RequestPartnerStoryFeed,
            value: Either[FE, ContentFeed[PublicContext]]) extends FM with MR[ContentFeed[PublicContext], RequestPartnerStoryFeed, FE]

case class GetCommunities() extends FM
case class GetCommunitiesResponse(
            message: GetCommunities,
            value: Either[FE, CommunityFeed]) extends FM with MR[CommunityFeed,GetCommunities, FE]

private[services] case class RequestTopicStoryFeed(topicId: String, page: Int) extends FM
private[services] case class RequestTopicStoryFeedResponse(
            message: RequestTopicStoryFeed,
            value: Either[FE, ContentFeed[PublicContext]]) extends FM with MR[ContentFeed[PublicContext], RequestTopicStoryFeed, FE]


case class RequestPublicContent(page: Int) extends FM
case class RequestPublicContentResponse(
            message: RequestPublicContent,
            value: Either[FE, ContentFeed[PublicContext]]) extends FM with MR[ContentFeed[PublicContext], RequestPublicContent, FE]
