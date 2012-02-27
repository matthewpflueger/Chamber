package com.echoed.chamber.services.partner

import org.slf4j.LoggerFactory
import akka.actor.{Channel, Actor}
import scala.reflect.BeanProperty
import com.echoed.util.Encrypter
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.{TransactionCallback, TransactionTemplate}
import com.echoed.chamber.services.email.EmailService
import com.echoed.util.TransactionUtils._
import scalaz._
import Scalaz._
import akka.dispatch.Future
import java.util.{Date, UUID, HashMap => JHashMap}
import com.echoed.chamber.dao.{EchoPossibilityDao, RetailerSettingsDao, RetailerDao, RetailerUserDao}

class PartnerServiceManagerActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[PartnerServiceManagerActor])

    @BeanProperty var partnerDao: RetailerDao = _
    @BeanProperty var partnerSettingsDao: RetailerSettingsDao = _
    @BeanProperty var partnerUserDao: RetailerUserDao = _
    @BeanProperty var echoPossibilityDao: EchoPossibilityDao = _
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

        case msg @ Locate(partnerId) =>
            val channel: Channel[LocateResponse] = self.channel

            val pf = Future {
                Option(partnerDao.findById(partnerId)).getOrElse(throw PartnerNotFound(partnerId))
            }
            val psf = Future {
                Option(partnerSettingsDao.findByActiveOn(partnerId, new Date())).getOrElse(throw PartnerNotActive(partnerId))
            }

            (for {
                partner <- pf
                partnerSettings <- psf
            } yield {
                channel ! LocateResponse(msg, Right(new PartnerServiceActorClient(Actor.actorOf(new PartnerServiceActor(
                        partner,
                        partnerSettings,
                        partnerDao,
                        partnerSettingsDao,
                        echoPossibilityDao,
                        encrypter)).start)))
            }).onException {
                case e: PartnerException => channel ! LocateResponse(msg, Left(e))
                case e => channel ! LocateResponse(msg, Left(PartnerException("Error locating partner %s" format partnerId, e)))
            }


//            Option(partnerDao.findById(partnerId)).cata(
//                partner => channel ! LocateResponse(msg, Right(new PartnerServiceActorClient(
//                        Actor.actorOf(new PartnerServiceActor(partner, partnerDao, encrypter)).start))),
//                channel ! LocateResponse(msg, Left(new PartnerNotFound(partnerId))))


    }

}
