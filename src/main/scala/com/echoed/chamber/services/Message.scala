package com.echoed.chamber.services

import com.echoed.util.UUID


trait Message extends Serializable {

    val id = UUID()
    val version = 1
    val correlation: Option[Message] = None

    var sentOn = System.currentTimeMillis()
    var receivedOn: Option[Long] = None

}









