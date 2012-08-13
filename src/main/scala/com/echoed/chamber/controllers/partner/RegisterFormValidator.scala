package com.echoed.chamber.controllers.partner

import org.springframework.validation.{Errors, Validator}
import java.lang.Class
import org.springframework.stereotype.Component


@Component
class RegisterFormValidator extends Validator {
    def supports(clazz: Class[_]) = clazz == classOf[RegisterForm]

    def validate(target: AnyRef, errors: Errors) {
        val form = target.asInstanceOf[RegisterForm]
        if (form.exhibitPercentage <= 0) errors.rejectValue("exhibitPercentage", "exhibitPercentage", "must be greater than 0")
        if (form.echoedMaxPercentage <= 0) errors.rejectValue("echoedMaxPercentage", "echoedMaxPercentage", "must be greater than 0")
        if (form.echoedMatchPercentage <= 0) errors.rejectValue("echoedMatchPercentage", "echoedMatchPercentage", "must be greater than 0")
        if (form.minClicks <= 0) errors.rejectValue("minClicks", "minClicks", "must be greater than 0")
        if (form.maxClicks <= 0) errors.rejectValue("maxClicks", "maxClicks", "must be greater than 0")
        if (form.creditWindow <= 0) errors.rejectValue("creditWindow", "creditWindow", "must be greater than 0")

        if (form.maxClicks <= form.minClicks) errors.rejectValue(
                "maxClicks",
                "maxClicks",
                Array[AnyRef]("min clicks", form.minClicks.asInstanceOf[AnyRef]),
                "must be greater than {0} of {1}")

        if (form.exhibitPercentage >= form.minPercentage) errors.rejectValue(
                "minPercentage",
                "minPercentage",
                Array[AnyRef]("exhibit percentage", form.exhibitPercentage.asInstanceOf[AnyRef]),
                "must be greater than {0} of {1}")

        if (form.maxPercentage <= form.minPercentage) errors.rejectValue(
                "maxPercentage",
                "maxPercentage",
                Array[AnyRef]("min percentage", form.minPercentage.asInstanceOf[AnyRef]),
                "must be greater than {0} of {1}")
    }
}
