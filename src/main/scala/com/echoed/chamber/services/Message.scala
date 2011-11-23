package com.echoed.chamber.services

import java.util.UUID


abstract case class Message(
        version: Int,
        id: String = UUID.randomUUID.toString,
        correlation: Option[Message] = None,
        sentOn: Long = System.currentTimeMillis(),
        var receivedOn: Option[Long] = None)









