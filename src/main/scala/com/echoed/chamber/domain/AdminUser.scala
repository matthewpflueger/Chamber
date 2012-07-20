package com.echoed.chamber.domain
import java.util.Date
import java.security.MessageDigest
import java.nio.charset.Charset
import org.apache.commons.codec.binary.Base64
import com.echoed.util.UUID

case class AdminUser(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        name: String,
        email: String,
        salt: String,
        password: String) {
    
    def this(id:String, name: String,  email:String) = this(
        id,
        new Date,
        new Date,
        name,
        email,
        List.fill(4)(UUID()).mkString,
        null
    )
    
    def this(name:String,email:String) = this(
        UUID(),
        new Date,
        new Date,
        name,
        email,
        List.fill(4)(UUID()).mkString,
        null)


    def isPassword(plainTextPassword: String) = {
        password == hash(plainTextPassword)
    }
    
    def createPassword(plainTextPassword: String) = {
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
