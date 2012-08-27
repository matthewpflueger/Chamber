package com.echoed.chamber.domain.partner

import java.util.Date
import com.echoed.util.UUID
import com.echoed.chamber.domain.{EmailPassword, DomainObject}
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
        @transient password: String) extends DomainObject with EmailPassword {

    def this() = this("", 0L, 0L, "", "", "", "", "")

    def this(partnerId: String, name: String, email: String) = this(
        UUID(),
        new Date,
        new Date,
        partnerId,
        name,
        email,
        null,
        null)

    def this(name: String, email: String) = this(
        null,
        name,
        email)


    def createPassword(plainTextPassword: String) = {
        val validatedPassword = plainTextPassword.trim()
        if (validatedPassword.length < 6) throw new InvalidPassword()

        val (s, p) = createSaltAndPassword(validatedPassword)
        copy(salt = s, password = p)
    }

}
