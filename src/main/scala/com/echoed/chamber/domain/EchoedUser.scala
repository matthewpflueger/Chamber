package com.echoed.chamber.domain


case class EchoedUser(
        id: String, // = UUID.randomUUID().toString,
        username: String,
        email: String,
        firstName: String,
        lastName: String,
        facebookUserId: String,
        twitterUserId: String) {

    def this(
            id: String,
            email: String,
            firstName: String,
            lastName: String) = this(id, null, email, firstName, lastName, null, null)

    def this(facebookUser: FacebookUser) = this(
            null,
            facebookUser.username,
            facebookUser.email,
            facebookUser.firstName,
            facebookUser.lastName,
            facebookUser.id,
            null)

    def this(twitterUser: TwitterUser) = this(
            null,
            twitterUser.username,
            "JonLWu@gmail.com",
            twitterUser.name,
            twitterUser.name,
            null,
            twitterUser.id)
}
