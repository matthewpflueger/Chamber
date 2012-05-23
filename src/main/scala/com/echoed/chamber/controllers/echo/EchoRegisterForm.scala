package com.echoed.chamber.controllers.echo

import org.hibernate.validator.constraints.{NotBlank, Email}

case class EchoRegisterForm(
    var email: String = null) {

    def this() = {
        this(null)
    }

    @Email
    @NotBlank
    def getEmail = email
    def setEmail(email: String) { this.email = email }
}
