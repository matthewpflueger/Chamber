package com.echoed.chamber.domain.views

import content.FeedItem
import context.Context

case class Feed[C <: Context](
    context:    C,
    content:    List[FeedItem],
    nextPage:   Option[Int]) {

}
