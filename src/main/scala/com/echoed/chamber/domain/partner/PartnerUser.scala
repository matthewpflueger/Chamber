package com.echoed.chamber.domain.partner

import java.util.Date
import java.nio.charset.Charset
import java.security.MessageDigest
import org.apache.commons.codec.binary.Base64
import com.echoed.util.UUID
import com.echoed.chamber.domain.DomainObject
import com.echoed.util.DateUtils._
import com.echoed.chamber.services.partneruser.InvalidPassword


case class PartnerUser(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        partnerId: String,
        name: String,
        email: String,
        @transient salt: String,
        @transient password: String) extends DomainObject {

    def this() = this("", 0L, 0L, "", "", "", "", "")

    def this(partnerId: String, name: String, email: String) = this(
        UUID(),
        new Date,
        new Date,
        partnerId,
        name,
        email,
        List.fill(4)(UUID()).mkString,
        null)

    def this(name: String, email: String) = this(
        null,
        name,
        email)

    def hasPassword = Option(password).isDefined

    def isCredentials(email: String, password: String) = this.email == email && isPassword(password)

    def isPassword(plainTextPassword: String) = {
        password == hash(plainTextPassword)
    }

    def createPassword(plainTextPassword: String) = {
        val ptp = plainTextPassword.trim()
        if (ptp.length < 6) throw new InvalidPassword()
        this.copy(password = hash(plainTextPassword))
    }

    private def hash(input: String) = {
        val charset = Charset.forName("UTF-8")
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update("108hqdvn9piqw3rfhy2e908bhb2p9834ufh9qw8fvh291348fhc9wqgu2hwq9s8ufy210r".getBytes(charset))
        messageDigest.update(input.getBytes(charset))
        messageDigest.update(salt.getBytes(charset))
        Base64.encodeBase64URLSafeString(messageDigest.digest())
    }
}

object PasswordGenerator extends App {
    def generate(password: String) {
        val pu = new PartnerUser("", "").createPassword(password)
        println("Salt: %s" format pu.salt)
        println("Pass: %s" format pu.password)
    }

    generate(args(0))
}