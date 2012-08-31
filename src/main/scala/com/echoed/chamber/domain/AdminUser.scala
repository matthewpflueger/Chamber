package com.echoed.chamber.domain
import java.util.Date
import java.security.MessageDigest
import java.nio.charset.Charset
import org.apache.commons.codec.binary.Base64
import com.echoed.util.UUID
import com.echoed.util.DateUtils._

case class AdminUser(
        id: String,
        updatedOn: Long,
        createdOn: Long,
        name: String,
        email: String,
        salt: String,
        password: String) extends DomainObject with EmailPassword {

    def this() = this("", 0L, 0L, "", "", "", "")

    def this(id: String, name: String,  email: String) = this(
        id,
        new Date,
        new Date,
        name,
        email,
        null,
        null
    )
    
    def this(name: String, email: String) = this(
        UUID(),
        new Date,
        new Date,
        name,
        email,
        null,
        null)


    def createPassword(plainTextPassword: String) = {
        val (s, p) = createSaltAndPassword(plainTextPassword)
        copy(salt = s, password = p)
    }

}
