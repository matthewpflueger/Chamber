package com.echoed.chamber.domain.partner

import java.util.Date
import com.echoed.util.UUID
import com.echoed.chamber.domain.{EmailPassword, DomainObject}
import com.echoed.util.DateUtils._


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
        val (s, p) = createSaltAndPassword(plainTextPassword)
        copy(salt = s, password = p)
    }

}
