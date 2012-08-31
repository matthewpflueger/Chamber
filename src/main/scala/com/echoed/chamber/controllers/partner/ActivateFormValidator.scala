package com.echoed.chamber.controllers.partner

import org.springframework.validation.{Errors, Validator}
import java.lang.Class
import org.springframework.stereotype.Component

@Component
class ActivateFormValidator extends Validator {
    def supports(clazz: Class[_]) = clazz == classOf[ActivateForm]

    def validate(target: AnyRef, errors: Errors) {
        val form = target.asInstanceOf[ActivateForm]
        if (form.password != form.passwordConfirm) errors.rejectValue("password", "passwords.invalid", "did not match")
    }
}
