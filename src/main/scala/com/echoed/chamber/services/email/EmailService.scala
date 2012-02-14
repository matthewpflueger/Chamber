package com.echoed.chamber.services.email

import akka.dispatch.Future
import java.util.{Map => JMap}


trait EmailService {

    def sendEmail(recipient: String, subject: String, view: String, model: JMap[String, AnyRef]): Future[SendEmailResponse]


}
