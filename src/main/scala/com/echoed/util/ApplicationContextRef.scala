package com.echoed.util

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ApplicationObjectSupport

class ApplicationContextRef extends ApplicationObjectSupport {
    override def isContextRequired = true

    override def initApplicationContext(context: ApplicationContext) {
        ApplicationContextRef.applicationContext = context
    }
}

object ApplicationContextRef {
    var applicationContext: ApplicationContext = _
}
