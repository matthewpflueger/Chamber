package com.echoed.chamber.services

import java.util.UUID


trait Message extends Serializable {

    val id = UUID.randomUUID.toString
    val version = 1
    val correlation: Option[Message] = None

    var sentOn = System.currentTimeMillis()
    var receivedOn: Option[Long] = None

}









