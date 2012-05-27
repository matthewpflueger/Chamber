package com.echoed.chamber.services.email

import reflect.BeanProperty

import org.slf4j.LoggerFactory

import akka.actor.{Channel, Actor}
import javax.mail.internet.MimeMessage
import org.springframework.mail.javamail.{MimeMessageHelper, MimeMessagePreparator, JavaMailSender}
import java.util.Properties
import com.echoed.util.mustache.MustacheEngine
import com.echoed.chamber.services.GlobalsManager


class EmailServiceActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[EmailServiceActor])

    @BeanProperty var javaMailSender: JavaMailSender = _
    @BeanProperty var mustacheEngine: MustacheEngine = _
    @BeanProperty var globalsManager: GlobalsManager = _
    @BeanProperty var from: String = _

    @BeanProperty var mailProperties: Properties = _

    override def preStart {
        {
            if (from == null) from = mailProperties.getProperty("mail.from")
            from != null
        } ensuring (_ == true, "Missing parameters")
    }

    def receive = {
        case msg @ SendEmail(recipient, subject, templateName, model) =>
            val channel: Channel[SendEmailResponse] = self.channel

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

}
