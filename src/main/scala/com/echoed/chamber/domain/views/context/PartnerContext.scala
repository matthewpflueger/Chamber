package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.public.{StoryPublic, PartnerPublic}
import com.echoed.chamber.domain.partner.Partner
import com.echoed.chamber.domain.public.Content

case class PartnerContext(
    partner: PartnerPublic,
    followers: Int,
    stories: Int,
    photos: Int,
    views: Int,
    votes: Int,
    comments: Int,
    mostCommented: Content,
    mostViewed: Content,
    mostVoted: Content
) extends Context {

    val id = partner.id
    val title = partner.name
    val contextType = "Partner"

    def this(partner: Partner,
             followers: Int,
             stories: Int,
             photos: Int,
             views: Int,
             votes: Int,
             comments: Int,
             mostCommented: Content,
             mostViewed: Content,
             mostVoted: Content) = this(new PartnerPublic(partner), followers, stories, photos, views, votes, comments, mostCommented, mostViewed, mostVoted)

}

