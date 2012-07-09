package com.echoed.chamber.services.email


import javax.mail.internet.MimeMessage
import org.springframework.mail.javamail.{MimeMessageHelper, MimeMessagePreparator, JavaMailSender}
import com.echoed.util.mustache.MustacheEngine
import com.echoed.chamber.services.GlobalsManager
import akka.actor._
import scala.Right
import scala.Left


class EmailServiceActor(
        javaMailSender: JavaMailSender,
        mustacheEngine: MustacheEngine,
        globalsManager: GlobalsManager,
        from: String) extends Actor with ActorLogging {

    require(from != null, "Missing from")

    def receive = {
        case msg @ SendEmail(recipient, subject, templateName, model) =>
            val channel = context.sender

            try {
                globalsManager.addGlobals(model)

                val renderedTemplate = mustacheEngine.execute(templateName, model) //compile(templateName).execute(model)

                javaMailSender.send(new MimeMessagePreparator() {
                    override def prepare(mimeMessage: MimeMessage) {
                        val mimeMessageHelper = new MimeMessageHelper(mimeMessage)
                        mimeMessageHelper.setTo(recipient)
                        mimeMessageHelper.setFrom(from)
                        mimeMessageHelper.setSubject(subject)
                        mimeMessageHelper.setText(renderedTemplate, true)
                    }
                })

                log.debug("Sent {}: {}", recipient, templateName)
                channel ! SendEmailResponse(msg, Right(true))
            } catch {
                case e =>
                    log.error("Error processing %s" format msg, e)
                    channel ! SendEmailResponse(msg, Left(EmailException("Failed to send email message", e)))
            }

    }

}
