package com.echoed.chamber.services.feed

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain.shopify.ShopifyOrderFull
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

case class GetPublicFeed(page: Int) extends FM
case class GetPublicFeedResponse(
            message: GetPublicFeed, 
            value: Either[FE, PublicFeed]) extends FM with RM[PublicFeed, GetPublicFeed, FE]


