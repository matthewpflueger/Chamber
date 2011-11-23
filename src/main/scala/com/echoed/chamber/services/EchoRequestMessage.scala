package com.echoed.chamber.services



case class EchoRequestMessage(
        echoedUserId: String,
        echoPossibilityId: String) extends Message(version = 1)

