package com.echoed.chamber.controllers

import reflect.BeanProperty

import com.echoed.chamber.services.echoeduser.EchoTo


case class EchoFinishParameters(
        @BeanProperty var message: String = null,
        @BeanProperty var postToFacebook: Boolean = false,
        @BeanProperty var postToTwitter: Boolean = false,
        @BeanProperty var echoedUserId: String = null,
        @BeanProperty var echoId: String = null) {

    def this() = this(
            null,
            false,
            false,
            null,
            null)

    def createEchoTo = new EchoTo(
            echoedUserId,
            echoId,
            Option(message),
            postToFacebook,
            Option(message),
            postToTwitter)
}


