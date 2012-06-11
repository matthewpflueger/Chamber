package com.echoed.chamber.services.email

import reflect.BeanProperty


import javax.mail.internet.MimeMessage
import org.springframework.mail.javamail.{MimeMessageHelper, MimeMessagePreparator, JavaMailSender}
import java.util.Properties
import com.echoed.util.mustache.MustacheEngine
import com.echoed.chamber.services.GlobalsManager
import org.springframework.beans.factory.FactoryBean
import akka.actor.{Props, ActorSystem, ActorRef, Actor}
import akka.util.duration._
import akka.util.Timeout
import akka.event.Logging


class EmailServiceActor extends FactoryBean[ActorRef] {


    @BeanProperty var javaMailSender: JavaMailSender = _
    @BeanProperty var mustacheEngine: MustacheEngine = _
    @BeanProperty var globalsManager: GlobalsManager = _
    @BeanProperty var from: String = _

    @BeanProperty var mailProperties: Properties = _

    @BeanProperty var timeoutInSeconds = 20
    @BeanProperty var actorSystem: ActorSystem = _

    def getObjectType = classOf[ActorRef]

    def isSingleton = true

    def getObject = actorSystem.actorOf(Props(new Actor {

    implicit val timeout = Timeout(timeoutInSeconds seconds)
    private final val logger = Logging(context.system, this)

    override def preStart {
        {
            if (from == null) from = mailProperties.getProperty("mail.from")
            from != null
        } ensuring (_ == true, "Missing parameters")
    }

    def receive = {
        case msg @ SendEmail(recipient, subject, templateName, model) =>
            val channel = context.sender

            try {
                globalsManager.addGlobals(model)

                val renderedTemplate = mustacheEngine.compile(templateName).execute(model)

                javaMailSender.send(new MimeMessagePreparator() {
                    override def prepare(mimeMessage: MimeMessage) {
                        val mimeMessageHelper = new MimeMessageHelper(mimeMessage)
                        mimeMessageHelper.setTo(recipient)
                        mimeMessageHelper.setFrom(from)
                        mimeMessageHelper.setSubject(subject)
                        mimeMessageHelper.setText(renderedTemplate, true)
                    }
                })

                logger.debug("Sent {}: {}", recipient, templateName)
                channel ! SendEmailResponse(msg, Right(true))
            } catch {
                case e =>
                    logger.error("Error processing %s" format msg, e)
                    channel ! SendEmailResponse(msg, Left(EmailException("Failed to send email message", e)))
            }

    }

    }), "EmailService")
}
