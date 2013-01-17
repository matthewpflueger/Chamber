package com.echoed.chamber.domain.views
import context.Context
import com.echoed.chamber.domain.public.Content

case class ContentFeed[C <: Context](
    context:    C,
    content:    List[Content],
    nextPage:   String) {

}
