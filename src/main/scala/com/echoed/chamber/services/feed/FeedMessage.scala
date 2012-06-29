package com.echoed.chamber.services.feed

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain.partner.shopify.ShopifyOrderFull
import com.echoed.chamber.domain.views._
import com.echoed.chamber.domain._


sealed trait FeedMessage extends Message

sealed case class FeedException(
                                message: String = "",
                                cause: Throwable = null,
                                code: Option[String] = None,
                                arguments: Option[Array[AnyRef]] = None) extends EchoedException(message, cause, code, arguments)

import com.echoed.chamber.services.feed.{FeedMessage => FM}
import com.echoed.chamber.services.feed.{FeedException => FE}

case class GetStory(storyId: String) extends FM
case class GetStoryResponse(message: GetStory, value: Either[FE, Option[StoryFull]])
        extends FM with RM[Option[StoryFull], GetStory, FE]

case class GetPublicFeed(page: Int) extends FM
case class GetPublicFeedResponse(
            message: GetPublicFeed, 
            value: Either[FE, PublicFeed]) extends FM with RM[PublicFeed, GetPublicFeed, FE]

case class GetPublicCategoryFeed(categoryId: String, page: Int) extends FM
case class GetPublicCategoryFeedResponse(
            message: GetPublicCategoryFeed,
            value: Either[FE, PublicFeed]) extends FM with RM [PublicFeed, GetPublicCategoryFeed, FE]

case class GetUserPublicFeed(echoedUserId: String, page: Int) extends FM
case class GetUserPublicFeedResponse(
            message: GetUserPublicFeed,
            value: Either[FE, EchoedUserFeed]) extends FM with RM[EchoedUserFeed, GetUserPublicFeed, FE]

case class GetPartnerFeed(partnerId: String, page: Int) extends FM
case class GetPartnerFeedResponse(
            message: GetPartnerFeed,
            value: Either[FE, PartnerFeed]) extends FM with RM[PartnerFeed, GetPartnerFeed, FE]

