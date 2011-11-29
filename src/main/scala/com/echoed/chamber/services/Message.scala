package com.echoed.chamber.services

import java.util.UUID


trait Message {

    def messageId = UUID.randomUUID.toString
    def messageVersion = 1
    def messageRoutingKey: Option[String] = None
    def messageCorrelation: Option[Message] = None

    var messageSentOn = System.currentTimeMillis()
    var messageReceivedOn: Option[Long] = None

}









