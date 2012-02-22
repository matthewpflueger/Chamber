package com.echoed.chamber.services.partner

import org.slf4j.LoggerFactory
import akka.actor.{Channel, Actor}
import scala.reflect.BeanProperty
import com.echoed.util.Encrypter
import org.springframework.dao.DataIntegrityViolationException
import com.echoed.chamber.dao.{RetailerSettingsDao, RetailerDao, RetailerUserDao}
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.{TransactionCallback, TransactionTemplate}
import com.echoed.chamber.services.email.EmailService
import com.echoed.util.TransactionUtils._
import java.util.{UUID, HashMap => JHashMap}

class PartnerServiceManagerActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[PartnerServiceManagerActor])

    @BeanProperty var partnerDao: RetailerDao = _
    @BeanProperty var partnerSettingsDao: RetailerSettingsDao = _
    @BeanProperty var partnerUserDao: RetailerUserDao = _
    @BeanProperty var encrypter: Encrypter = _
    @BeanProperty var transactionTemplate: TransactionTemplate = _
    @BeanProperty var emailService: EmailService = _


    def receive = {
        case msg @ RegisterPartner(partner, partnerSettings, partnerUser) =>
            val channel: Channel[RegisterPartnerResponse] = self.channel

            def error(e: Throwable) {
                logger.error("Unexpected error processing %s" format msg, e)
                e match {
                    case e: DataIntegrityViolationException =>
                        channel ! RegisterPartnerResponse(msg, Left(PartnerException(e.getCause.getMessage, e)))
                    case e: PartnerException =>
                        channel ! RegisterPartnerResponse(msg, Left(e))
                    case e =>
                        channel ! RegisterPartnerResponse(msg, Left(PartnerException("Could not register %s" format partner.name, e)))
                }
            }

            try {
                val p = partner.copy(secret = encrypter.generateSecretKey)
                val ps = partnerSettings.copy(retailerId = p.id)
                val password = UUID.randomUUID().toString
                val pu = partnerUser.copy(retailerId = p.id).createPassword(password)
                val code = encrypter.encrypt("""{"email": "%s", "password": "%s"}""" format (partnerUser.email, password))


                transactionTemplate.execute({status: TransactionStatus =>
                        partnerDao.insert(p)
                        partnerSettingsDao.insert(ps)
                        partnerUserDao.insert(pu)
                })

                channel ! RegisterPartnerResponse(msg, Right(p))

                val model = new JHashMap[String, AnyRef]()
                model.put("code", code)
                model.put("partner", p)
                model.put("partnerUser", pu)
                emailService.sendEmail(
                    partnerUser.email,
                    "Your Echoed Account",
                    "partner_email_register",
                    model)
            } catch {
                case e => error(e)
            }

    }

}
