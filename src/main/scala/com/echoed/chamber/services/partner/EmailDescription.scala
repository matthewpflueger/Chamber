package com.echoed.chamber.services.partner

case class EmailDescription(templateName: String, subject: String, model: Option[Map[String, AnyRef]] = None)

