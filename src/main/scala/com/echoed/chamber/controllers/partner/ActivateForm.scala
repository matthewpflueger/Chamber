package com.echoed.chamber.controllers.partner

import org.hibernate.validator.constraints.{Length, NotBlank}


case class ActivateForm(
        var partnerUserId: String = null,
        var password: String = null,
        var passwordConfirm: String = null) {

    def this() = {
        this(null, null, null)
    }

    def this(partnerUserId: String) = this(partnerUserId, null, null)

    @NotBlank
    def getPartnerUserId = partnerUserId
    def setPartnerUserId(partnerUserId: String) { this.partnerUserId = partnerUserId }

    @NotBlank
    @Length(min = 6, max = 200, message="{password.invalid}")
    def getPassword = password
    def setPassword(password: String) { this.password = password }

    @NotBlank
    @Length(min = 6, max = 200, message="{password.invalid}")
    def getPasswordConfirm = passwordConfirm
    def setPasswordConfirm(passwordConfirm: String) { this.passwordConfirm = passwordConfirm }
}
