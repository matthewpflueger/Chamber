package com.echoed.chamber.domain

import java.util.{UUID, Date}
import java.security.MessageDigest
import java.nio.charset.Charset
import org.apache.commons.codec.binary.Base64
import com.echoed.chamber.services.partneruser.InvalidPassword
import com.echoed.chamber.domain.shopify.ShopifyPartner

case class PartnerUser(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        partnerId: String,
        name: String,
        email: String,
        @transient salt: String,
        @transient password: String) {
    

    def this(partnerId: String, name: String, email: String) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        partnerId,
        name,
        email,
        UUID.randomUUID.toString + UUID.randomUUID.toString + UUID.randomUUID.toString + UUID.randomUUID.toString,
        null)

    def this(name: String, email: String) = this(
        null,
        name,
        email)

    val hasPassword = Option(password) != None

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

