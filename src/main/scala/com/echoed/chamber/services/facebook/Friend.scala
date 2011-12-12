package com.echoed.chamber.services.facebook

import com.echoed.chamber.domain.FacebookFriend


case class Friend(
        id: String,
        name: String) {

    def createFacebookFriend(facebookUserId: String) = new FacebookFriend(
        facebookUserId,
        id,
        name)
}
