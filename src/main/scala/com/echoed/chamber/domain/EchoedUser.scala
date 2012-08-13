package com.echoed.chamber.domain

import java.util.Date
import com.echoed.util.UUID
import com.echoed.util.DateUtils._
import com.fasterxml.jackson.annotation.{JsonProperty, JsonCreator}


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
        twitterId: String) extends DomainObject {

    def this() = this("", 0L, 0L, "", "", "", "", "", "", "")

    def this(
            name: String,
            email: String,
            screenName: String,
            facebookUserId: String,
            facebookId: String,
            twitterUserId: String,
            twitterId: String) = this(
        UUID(),
        new Date,
        new Date,
        name,
        email,
        screenName,
        facebookUserId,
        facebookId,
        twitterUserId,
        twitterId)


    def this(
            id:String,
            name:String,
            email:String,
            screenName: String,
            facebookUserId: String,
            facebookId:String,
            twitterUserId: String,
            twitterId: String) = this(
        id,
        new Date,
        new Date,
        name,
        email,
        screenName,
        facebookUserId,
        facebookId,
        twitterUserId,
        twitterId)


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
        null)

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
        twitterUser.twitterId)

    def assignFacebookUser(fu: FacebookUser) =
        this.copy(facebookId = fu.facebookId, facebookUserId = fu.id, email = Option(email).getOrElse(fu.email))

    def assignTwitterUser(tu: TwitterUser) =
        this.copy(twitterId = tu.twitterId, twitterUserId = tu.id, screenName = tu.screenName)

    def hasEmail = Option(email).isDefined
}


