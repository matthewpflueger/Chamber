package com.echoed.chamber.controllers.echo


import org.springframework.validation.{Errors, Validator}
import java.lang.Class


class EchoRegisterFormValidator extends Validator{

    def supports(clazz: Class[_]) = clazz == classOf[EchoRegisterForm]

    def validate(target: AnyRef, errors: Errors) {
    }

}
