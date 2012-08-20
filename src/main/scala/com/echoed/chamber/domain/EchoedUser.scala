package com.echoed.chamber.domain

import java.util.Date
import com.echoed.util.UUID
import com.echoed.util.DateUtils._
import com.fasterxml.jackson.annotation.{JsonProperty, JsonCreator}
import java.nio.charset.Charset
import java.security.MessageDigest
import org.apache.commons.codec.binary.Base64


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
        salt: String) extends DomainObject {

    def this() = this("", 0L, 0L, "", "", "", "", "", "", "", "", "")

    def this(name: String, email: String) = this(
        UUID(),
        new Date,
        new Date,
        name,
        email,
        null,
        null,
        null,
        null,
        null,
        null,
        null)

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
        twitterUser.twitterId,
        null,
        null)

    def assignFacebookUser(fu: FacebookUser) =
        this.copy(facebookId = fu.facebookId, facebookUserId = fu.id, email = Option(email).getOrElse(fu.email))

    def assignTwitterUser(tu: TwitterUser) =
        this.copy(twitterId = tu.twitterId, twitterUserId = tu.id, screenName = tu.screenName)

    def hasEmail = Option(email).isDefined
    def hasPassword = Option(password).isDefined

    def isCredentials(email: String, password: String) = this.email == email && isPassword(password)

    def isPassword(plainTextPassword: String) = {
        if (Option(salt).isEmpty || Option(password).isEmpty) false
        else password == hash(plainTextPassword)
    }

    def createPassword(plainTextPassword: String) = {
        val eu = this.copy(salt = List.fill(4)(UUID()).mkString)
        eu.copy(password = eu.hash(plainTextPassword))
    }

    private def hash(input: String) = {
        val charset = Charset.forName("UTF-8")
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update("asihgv912348goqenr20348tywudfbv9q8e4wtghy9ervubwd98u8fbuq9w84hg9qhv9qe".getBytes(charset))
        messageDigest.update(input.getBytes(charset))
        messageDigest.update(salt.getBytes(charset))
        Base64.encodeBase64URLSafeString(messageDigest.digest())
    }
}


