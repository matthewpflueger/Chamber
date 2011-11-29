package com.echoed.chamber.services.echo

import com.echoed.chamber.services.Message

case class EchoRequestMessage(
        echoedUserId: String,
        echoPossibilityId: String,
        facebookMessage: Option[String] = None,
        postToFacebook: Boolean = false,
        twitterMessage: Option[String] = None,
        postToTwitter: Boolean = false) extends Message


