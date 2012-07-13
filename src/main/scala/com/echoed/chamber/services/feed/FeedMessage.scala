package com.echoed.chamber.services.feed

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain.views._


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
        value: Either[FE, Array[String]]) extends FM with RM[Array[String], GetPartnerIds, FE]

case class GetStoryIds() extends FM
case class GetStoryIdsResponse(message: GetStoryIds, value: Either[FE, Array[String]])
        extends FM with RM[Array[String], GetStoryIds, FE]

case class GetStory(storyId: String) extends FM
case class GetStoryResponse(message: GetStory, value: Either[FE, Option[StoryFull]])
        extends FM with RM[Option[StoryFull], GetStory, FE]

case class GetPublicFeed(page: Int) extends FM
case class GetPublicFeedResponse(
            message: GetPublicFeed, 
            value: Either[FE, PublicFeed]) extends FM with RM[PublicFeed, GetPublicFeed, FE]

case class GetPublicStoryFeed(page: Int) extends FM
case class GetPublicStoryFeedResponse(
            message: GetPublicStoryFeed,
            value: Either[FE, PublicStoryFeed]) extends FM with RM[PublicStoryFeed, GetPublicStoryFeed, FE]

case class GetPublicCategoryFeed(categoryId: String, page: Int) extends FM
case class GetPublicCategoryFeedResponse(
            message: GetPublicCategoryFeed,
            value: Either[FE, PublicFeed]) extends FM with RM [PublicFeed, GetPublicCategoryFeed, FE]

case class GetUserPublicFeed(echoedUserId: String, page: Int) extends FM
case class GetUserPublicFeedResponse(
            message: GetUserPublicFeed,
            value: Either[FE, EchoedUserFeed]) extends FM with RM[EchoedUserFeed, GetUserPublicFeed, FE]

case class GetUserPublicStoryFeed(echoedUserId: String, page: Int) extends FM
case class GetUserPublicStoryFeedResponse(
            message: GetUserPublicStoryFeed,
            value: Either[FE, EchoedUserStoryFeed]) extends FM with RM[EchoedUserStoryFeed, GetUserPublicStoryFeed, FE]

case class GetPartnerFeed(partnerId: String, page: Int) extends FM
case class GetPartnerFeedResponse(
            message: GetPartnerFeed,
            value: Either[FE, PartnerFeed]) extends FM with RM[PartnerFeed, GetPartnerFeed, FE]

case class GetPartnerStoryFeed(partnerId: String, page: Int) extends FM
case class GetPartnerStoryFeedResponse(
            message: GetPartnerStoryFeed,
            value: Either[FE, PartnerStoryFeed]) extends FM with RM[PartnerStoryFeed, GetPartnerStoryFeed, FE]