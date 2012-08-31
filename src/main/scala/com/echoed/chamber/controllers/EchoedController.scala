package com.echoed.chamber.controllers

import com.echoed.util.{Encrypter, Logging}
import com.echoed.chamber.services.{EventProcessor, MessageProcessor}
import org.springframework.beans.factory.annotation.Autowired

abstract class EchoedController {
    protected val log = Logging(this.getClass)

    @Autowired protected var v: Views = _
    @Autowired protected var mp: MessageProcessor = _
    @Autowired protected var ep: EventProcessor = _
    @Autowired protected var cookieManager: CookieManager = _
    @Autowired protected var encrypter: Encrypter = _
}
