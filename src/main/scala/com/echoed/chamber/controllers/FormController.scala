package com.echoed.chamber.controllers

import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.bind.WebDataBinder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.Validator
import org.springframework.core.convert.ConversionService

trait FormController {

    def defaultFieldPrefix: String = "form"

    @Autowired var validator: Validator = _
    @Autowired var conversionService: ConversionService = _

    @InitBinder
    def initBinder(binder: WebDataBinder) {
        binder.setFieldDefaultPrefix(defaultFieldPrefix)
        binder.setValidator(validator)
        binder.setConversionService(conversionService)
    }


}
