package com.echoed.chamber.domain.views.context

import com.echoed.chamber.domain.public.{StoryPublic, EchoedUserPublic, Content}
import com.echoed.chamber.domain.EchoedUser

case class UserContext(
    echoedUser: EchoedUserPublic,
    followers: Int,
    following: Int,
    stories: Int,
    photos: Int,
    views: Int,
    votes: Int,
    comments: Int,
    mostCommented: Content,
    mostViewed: Content,
    mostVoted: Content

) extends Context {

    val id = echoedUser.id
    val title = echoedUser.name
    val contextType = "User"

    def this(echoedUser: EchoedUser,
             followers: Int,
             following: Int,
             stories: Int,
             photos: Int,
             views: Int,
             votes: Int,
             comments: Int,
             mostCommented: Content,
             mostViewed: Content,
             mostVoted: Content) = this( new EchoedUserPublic(echoedUser), followers, following, stories, photos, views, votes, comments, mostCommented, mostViewed, mostVoted)

}
