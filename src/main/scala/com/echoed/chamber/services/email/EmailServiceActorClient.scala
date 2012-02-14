package com.echoed.chamber.services.email

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.services.ActorClient
import java.util.{Map => JMap}


class EmailServiceActorClient extends EmailService with ActorClient {

    @BeanProperty var actorRef: ActorRef = _

    def sendEmail(recipient: String, subject: String, view: String, model: JMap[String, AnyRef]) =
        (actorRef ? SendEmail(recipient, subject, view, model)).mapTo[SendEmailResponse]

}
