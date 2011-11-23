package com.echoed.chamber.services

import java.util.UUID


abstract case class ErrorMessage(
        version: Int,
        description: Option[String] = None) extends Message(version)









