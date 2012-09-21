package com.echoed.chamber.controllers.partner

import org.hibernate.validator.constraints.{Length, NotBlank, Email}
import com.echoed.chamber.services.partneruser.PartnerUserClientCredentials

case class UpdatePartnerUserForm(
        var name: String,
        var email: String,
        var password: String) {

    def this() = {
        this("", "", "")
    }

    def this(pucc: PartnerUserClientCredentials) = this(pucc.name.orNull, pucc.email.orNull, "")

    @Email
    @NotBlank
    def getEmail = email
    def setEmail(email: String) { this.email = email }

    @NotBlank
    def getName = name
    def setName(name: String) { this.name = name }

    @NotBlank
    @Length(min = 6, max = 200, message="{password.invalid}")
    def getPassword = password
    def setPassword(password: String) { this.password = password }

}

