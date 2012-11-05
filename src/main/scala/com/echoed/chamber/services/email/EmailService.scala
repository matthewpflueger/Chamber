package com.echoed.chamber.services.email


import javax.mail.internet.MimeMessage
import org.springframework.mail.javamail.{MimeMessageHelper, MimeMessagePreparator, JavaMailSender}
import com.echoed.util.mustache.MustacheEngine
import com.echoed.chamber.services.{EchoedService, GlobalsManager}
import scala.Right
import scala.collection.JavaConversions._



class EmailService(
        javaMailSender: JavaMailSender,
        mustacheEngine: MustacheEngine,
        globalsManager: GlobalsManager,
        from: String,
        fromName: String = "Echoed",
        dropMessages: Boolean = false,
        authorizedAddresses: String = "*") extends EchoedService {

    require(from != null, "Missing from")

    def handle = {
        case msg @ SendEmail(recipient, subject, templateName, m)
                if (!dropMessages && (authorizedAddresses == "*" || authorizedAddresses.contains(recipient))) =>

            val renderedTemplate = mustacheEngine.execute(templateName, globalsManager.globals() ++ m)

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
            sender ! SendEmailResponse(msg, Right(true))
    }

}
