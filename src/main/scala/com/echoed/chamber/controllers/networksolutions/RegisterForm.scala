package com.echoed.chamber.controllers.networksolutions

import com.echoed.chamber.domain.{PartnerSettings, PartnerUser, Partner}
import java.util.{UUID, Date}
import org.hibernate.validator.constraints.{NotBlank, Email, URL}
import org.springframework.format.annotation.NumberFormat.Style
import javax.validation.constraints._
import org.springframework.format.annotation.{DateTimeFormat, NumberFormat}
import org.springframework.format.annotation.DateTimeFormat.ISO

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
