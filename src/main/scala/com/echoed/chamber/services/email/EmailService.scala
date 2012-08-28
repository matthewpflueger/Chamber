package com.echoed.chamber.services.email


import javax.mail.internet.MimeMessage
import org.springframework.mail.javamail.{MimeMessageHelper, MimeMessagePreparator, JavaMailSender}
import com.echoed.util.mustache.MustacheEngine
import com.echoed.chamber.services.{EchoedService, GlobalsManager}
import java.util.{HashMap => JMap}
import scala.Right
import scala.Left


class EmailService(
        javaMailSender: JavaMailSender,
        mustacheEngine: MustacheEngine,
        globalsManager: GlobalsManager,
        from: String,
        fromName: String = "Echoed") extends EchoedService {

    require(from != null, "Missing from")

    def handle = {
        case msg @ SendEmail(recipient, subject, templateName, m) =>
            val channel = context.sender
            val model = new JMap[String, AnyRef](m)

            try {
                globalsManager.addGlobals(model)

                val renderedTemplate = mustacheEngine.execute(templateName, model)

                javaMailSender.send(new MimeMessagePreparator() {
                    override def prepare(mimeMessage: MimeMessage) {
                        val mimeMessageHelper = new MimeMessageHelper(mimeMessage)
                        mimeMessageHelper.setTo(recipient)
                        mimeMessageHelper.setFrom(from, fromName)
                        mimeMessageHelper.setSubject(subject)
                        mimeMessageHelper.setText(renderedTemplate, true)
                    }
                })

                log.debug("Sent {}: {}", recipient, templateName)
                channel ! SendEmailResponse(msg, Right(true))
            } catch {
                case e =>
                    log.error(e, "Error processing {}", msg)
                    channel ! SendEmailResponse(msg, Left(EmailException("Failed to send email message", e)))
            }

    }

}
