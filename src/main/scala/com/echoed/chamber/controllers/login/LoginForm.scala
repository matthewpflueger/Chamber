package com.echoed.chamber.controllers.login

import org.hibernate.validator.constraints.{Length, NotBlank, Email}
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import scala.reflect.BeanProperty


class LoginForm extends CredForm with PasswordForm

class LoginRegisterForm(eucc: Option[EchoedUserClientCredentials] = None) extends EmailForm with PasswordForm {

    def this() = this(None)

    eucc.map { c =>
        this.echoedUserId = c.id
        this.name = c.name.orNull
        this.email = c.email.orNull
        this.screenName = c.screenName.orNull
        this.password = c.password.orNull
        this.originalPassword = c.password.orNull
    }

    @BeanProperty var echoedUserId: String = _
    @BeanProperty var originalPassword: String = _

    var name: String = _

    @NotBlank
    def getName = name
    def setName(name: String) {
        this.name = name
    }

    var screenName: String = _

    @NotBlank
    def getScreenName = screenName
    def setScreenName(screenName: String) {
        this.screenName = screenName
    }
}

class LoginResetForm extends CredForm

class ResetPasswordForm extends PasswordForm


trait CredForm {

    var cred: String = _

    @NotBlank
    def getCred = cred
    def setCred(cred: String) {
        this.cred = cred
    }
}


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
    @Length(min = 6, message = "length must be at least 6 characters")
    def getPassword = password
    def setPassword(password: String) {
        this.password = password
    }
}
