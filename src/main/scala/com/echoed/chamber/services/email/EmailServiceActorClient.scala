package com.echoed.chamber.services.email

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.services.ActorClient
import java.util.{Map => JMap}
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._


class EmailServiceActorClient extends EmailService with ActorClient {

    @BeanProperty var actorRef: ActorRef = _

    private implicit val timeout = Timeout(20 seconds)

    def sendEmail(recipient: String, subject: String, view: String, model: JMap[String, AnyRef]) =
        (actorRef ? SendEmail(recipient, subject, view, model)).mapTo[SendEmailResponse]

}
