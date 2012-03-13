package com.echoed.chamber.controllers

import reflect.BeanProperty

import com.echoed.chamber.services.echoeduser.EchoTo


case class EchoFinishParameters(
        @BeanProperty var facebookMessage: String = null,
        @BeanProperty var postToFacebook: Boolean = false,
        @BeanProperty var twitterMessage: String = null,
        @BeanProperty var postToTwitter: Boolean = false,
        @BeanProperty var echoedUserId: String = null,
        @BeanProperty var echoId: String = null) {

    def this() = this(
            null,
            false,
            null,
            false,
            null,
            null)

    def createEchoTo = new EchoTo(
            echoedUserId,
            echoId,
            Option(facebookMessage),
            postToFacebook,
            Option(twitterMessage),
            postToTwitter)
}

