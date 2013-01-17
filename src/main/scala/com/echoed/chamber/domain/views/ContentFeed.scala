package com.echoed.chamber.domain.views

import content.Content
import context.Context

case class ContentFeed[C <: Context](
    context:    C,
    content:    List[Content],
    nextPage:   String) {

}
