package com.echoed.chamber.domain

import java.nio.charset.Charset
import java.security.MessageDigest
import com.echoed.util.UUID
import org.apache.commons.codec.binary.Base64
import com.echoed.chamber.services.EchoedException

trait EmailPassword { self =>
    def email: String
    def password: String
    def salt: String

    def hasEmail = Option(email).isDefined
    def hasPassword = Option(password).isDefined

    def isCredentials(email: String, password: String) = self.email == email && isPassword(password)

    def isPassword(plainTextPassword: String) = {
        if (Option(salt).isEmpty || Option(password).isEmpty) false
        else password == hash(plainTextPassword)
    }

    protected final def createSaltAndPassword(plainTextPassword: String) = {
        val validatedPassword = validatePassword(plainTextPassword)
        val newSalt = List.fill(4)(UUID()).mkString
        (newSalt, hash(validatedPassword, Option(newSalt)))
    }

    def validatePassword(plainTextPassword: String) = {
        val validatedPassword = plainTextPassword.trim()
        if (validatedPassword.length < 6) throw new InvalidPassword()
        validatedPassword
    }

    private def hash(input: String, newSalt: Option[String] = None) = {
        val charset = Charset.forName("UTF-8")
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update("108hqdvn9piqw3rfhy2e908bhb2p9834ufh9qw8fvh291348fhc9wqgu2hwq9s8ufy210r".getBytes(charset))
        messageDigest.update(input.getBytes(charset))
        messageDigest.update(newSalt.getOrElse(salt).getBytes(charset))
        Base64.encodeBase64URLSafeString(messageDigest.digest())
    }
}

case class InvalidPassword(
        message: String = "Invalid password",
        cause: Throwable = null,
        code: Option[String] = Some("password.invalid")) extends EchoedException(message, cause, code)


object PasswordGenerator extends App {
    def generate(plainTextPassword: String) {
        val (salt, password) = new EmailPassword() {
            val email = null
            val password = plainTextPassword
            val salt = null

            def createPassword(plainTextPassword: String) = createSaltAndPassword(plainTextPassword)
        }.createPassword(plainTextPassword)

        println("Salt: %s" format salt)
        println("Password: %s" format password)
    }

    generate(args(0))
}