package com.echoed.chamber.domain

import java.util.Date
import com.echoed.util.UUID
import com.echoed.util.DateUtils._


case class EchoedUser(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        name: String,
        email: String,
        screenName: String,
        facebookUserId: String,
        facebookId: String,
        twitterUserId: String,
        twitterId: String,
        password: String,
        salt: String,
        emailVerified: Boolean) extends DomainObject with EmailPassword {

    def this() = this("", 0L, 0L, "", "", "", "", "", "", "", "", "", false)

    def this(name: String, email: String, screenName: String) = this(
        UUID(),
        new Date,
        new Date,
        name,
        email,
        screenName,
        null,
        null,
        null,
        null,
        null,
        null,
        false)

    def this(facebookUser: FacebookUser) = this(
        UUID(),
        new Date,
        new Date,
        facebookUser.name,
        facebookUser.email,
        null,
        facebookUser.id,
        facebookUser.facebookId,
        null,
        null,
        null,
        null,
        true)

    def this(twitterUser: TwitterUser) = this(
        UUID(),
        new Date,
        new Date,
        twitterUser.name,
        null,
        twitterUser.screenName,
        null,
        null,
        twitterUser.id,
        twitterUser.twitterId,
        null,
        null,
        false)

    def assignFacebookUser(fu: FacebookUser) =
        this.copy(facebookId = fu.facebookId, facebookUserId = fu.id, email = Option(email).getOrElse(fu.email))

    def assignTwitterUser(tu: TwitterUser) =
        this.copy(twitterId = tu.twitterId, twitterUserId = tu.id, screenName = Option(screenName).getOrElse(tu.screenName))

    def createPassword(plainTextPassword: String) = {
        val (s, p) = createSaltAndPassword(plainTextPassword)
        copy(salt = s, password = p)
    }

    override def isCredentials(cred: String, password: String) =
        (this.screenName == cred && isPassword(password)) || super.isCredentials(cred, password)
}


