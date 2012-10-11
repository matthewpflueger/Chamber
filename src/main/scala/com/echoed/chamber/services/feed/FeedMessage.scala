package com.echoed.chamber.services.feed

import com.echoed.chamber.services.{MessageResponse => MR, Event, EchoedException, Message}
import com.echoed.chamber.domain.views._
import com.echoed.chamber.domain.public.StoryPublic
import com.echoed.chamber.domain.Community

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
        value: Either[FE, Array[String]]) extends FM with MR[Array[String], GetPartnerIds, FE]

case class GetStoryIds() extends FM
case class GetStoryIdsResponse(message: GetStoryIds, value: Either[FE, Array[String]])
        extends FM with MR[Array[String], GetStoryIds, FE]

case class GetStory(storyId: String, origin: String) extends FM
case class GetStoryResponse(message: GetStory, value: Either[FE, Option[StoryPublic]])
        extends FM with MR[Option[StoryPublic], GetStory, FE]

case class GetPublicStoryFeed(page: Int) extends FM
case class GetPublicStoryFeedResponse(
            message: GetPublicStoryFeed,
            value: Either[FE, PublicStoryFeed]) extends FM with MR[PublicStoryFeed, GetPublicStoryFeed, FE]

case class GetCategoryStoryFeed(categoryId: String, page: Int) extends FM
case class GetCategoryStoryFeedResponse(
            message: GetCategoryStoryFeed,
            value: Either[FE, PublicStoryFeed]) extends FM with MR [PublicStoryFeed, GetCategoryStoryFeed, FE]

case class GetUserPublicStoryFeed(echoedUserId: String, page: Int) extends FM
case class GetUserPublicStoryFeedResponse(
            message: GetUserPublicStoryFeed,
            value: Either[FE, EchoedUserStoryFeed]) extends FM with MR[EchoedUserStoryFeed, GetUserPublicStoryFeed, FE]

case class GetPartnerStoryFeed(partnerId: String, page: Int, origin: String) extends FM
case class GetPartnerStoryFeedResponse(
            message: GetPartnerStoryFeed,
            value: Either[FE, PartnerStoryFeed]) extends FM with MR[PartnerStoryFeed, GetPartnerStoryFeed, FE]


case class GetCommunities() extends FM
case class GetCommunitiesResponse(
            message: GetCommunities,
            value: Either[FE, CommunityFeed]) extends FM with MR[CommunityFeed,GetCommunities, FE]

