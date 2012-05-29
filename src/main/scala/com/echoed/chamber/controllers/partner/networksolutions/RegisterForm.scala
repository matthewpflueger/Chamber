package com.echoed.chamber.controllers.partner.networksolutions

import org.hibernate.validator.constraints.{NotBlank, Email}

case class RegisterForm(
        var name: String = null,
        var email: String = null,
        var phone: String = null) {


    def this() = {
        this(null)
    }


    @Email
    @NotBlank
    def getEmail = email
    def setEmail(email: String) {
        this.email = email
    }

    @NotBlank
    def getName = name
    def setName(name: String) {
        this.name = name
    }

    @NotBlank
    def getPhone = phone
    def setPhone(phone: String) {
        this.phone = phone
    }


}
