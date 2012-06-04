package com.echoed.chamber.controllers.api.admin

import org.springframework.validation.{ Errors, Validator }

class AdminUpdatePartnerSettingsFormValidator  extends Validator{

    def supports(clazz: Class[_]) = clazz == classOf[AdminUpdatePartnerSettingsForm]

    def validate(target: AnyRef, errors: Errors) {
    }

}
