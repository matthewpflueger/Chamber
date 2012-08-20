package com.echoed.chamber.controllers.login

import org.hibernate.validator.constraints.{Length, NotBlank, Email}


class LoginForm extends EmailForm with PasswordForm

class LoginRegisterForm extends EmailForm with PasswordForm {

    var name: String = null

    @NotBlank
    def getName = name
    def setName(name: String) {
        this.name = name
    }
}

class LoginResetForm extends EmailForm

class ResetPasswordForm extends PasswordForm


trait EmailForm {

    var email: String = _

    @Email
    @NotBlank
    def getEmail = email
    def setEmail(email: String) {
        this.email = email
    }
}


trait PasswordForm {

    var password: String = _

    @NotBlank
    @Length(min = 4, message = "length must be at least 4 characters")
    def getPassword = password
    def setPassword(password: String) {
        this.password = password
    }
}
