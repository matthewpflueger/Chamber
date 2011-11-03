package com.echoed.chamber.domain

import reflect.BeanProperty
import java.util.Date

case class RetailerConfirmation(
        @BeanProperty var id: String,
        @BeanProperty var retailerId: String,
        @BeanProperty var productId: String,
        @BeanProperty var shownOn: Date,
        @BeanProperty var step: Int) {

    def this() = {
        this("", "", "", new Date, -1)
    }

}
