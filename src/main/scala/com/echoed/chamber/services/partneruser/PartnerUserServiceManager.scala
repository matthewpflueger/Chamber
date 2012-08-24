package com.echoed.chamber.services.partneruser

import akka.actor._
import akka.util.Timeout
import com.echoed.chamber.services._
import com.echoed.chamber.domain.Identifiable
import com.google.common.collect.HashMultimap
import akka.actor.Terminated
import scala.collection.JavaConversions._
import scalaz._
import Scalaz._
import com.echoed.util.{Encrypter, ScalaObjectMapper}


class PartnerUserServiceManager(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        partnerUserServiceCreator: (ActorContext, Message) => ActorRef,
        encrypter: Encrypter,
        implicit val timeout: Timeout = Timeout(20000)) extends EchoedService {

    private val active = HashMultimap.create[Identifiable, ActorRef]()


    def handle = {
        case Terminated(ref) => active.values.removeAll(active.values.filter(_ == ref))

        case RegisterPartnerUserService(partnerUser) =>
            active.put(PartnerUserId(partnerUser.id), context.sender)
            active.put(Email(partnerUser.email), context.sender)

        case msg @ LoginWithCode(code) =>
            val channel = context.sender
            val map = ScalaObjectMapper(encrypter.decrypt(code), classOf[Map[String, String]])
            mp(LoginWithEmailPassword(map("email"), map("password"))).onSuccess {
                case LoginWithEmailPasswordResponse(_, Left(e)) =>
                    channel ! LoginWithCodeResponse(msg, Left(e))
                case LoginWithEmailPasswordResponse(_, Right(echoedUser)) =>
                    channel ! LoginWithCodeResponse(msg, Right(echoedUser))
            }

        case msg: EmailIdentifiable with PartnerUserMessage =>
            active.get(Email(msg.email)).headOption.cata(
                _.forward(msg),
                {
                    val partnerUserService = context.watch(partnerUserServiceCreator(context, LoginWithEmail(msg.email, msg, Option(sender))))
                    partnerUserService.forward(msg)
                    active.put(Email(msg.email), partnerUserService)
                })


        case msg: PartnerUserIdentifiable =>
            active.get(PartnerUserId(msg.partnerUserId)).headOption.cata(
                _.forward(msg),
                {
                    val partnerUserService = context.watch(partnerUserServiceCreator(context, LoginWithCredentials(msg.credentials)))
                    partnerUserService.forward(msg)
                    active.put(PartnerUserId(msg.partnerUserId), partnerUserService)
                })

    }

/*
    private val cache: ConcurrentMap[String, PartnerUserService] = new ConcurrentHashMap[String, PartnerUserService]()
    private var cacheById = cacheManager.getCache[PartnerUserService]("PartnerUserServices", Some(new CacheListenerActorClient(self)))

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: Exception ⇒ Restart
    }

    def login(msg: Login, channel: ActorRef) {
        val email = msg.email
        val password = msg.password
        log.debug("Locating PartnerService for {}", email)

        cache.get(email) match {
            case Some(partnerUserService) =>
                log.debug("Cache hit for {}", email)
                partnerUserService.getPartnerUser.onSuccess {
                    case GetPartnerUserResponse(_, Right(pu)) =>
                        if (pu.isPassword(password)) {
                            cacheById.put(pu.id, partnerUserService)
                            channel ! LoginResponse(msg, Right(partnerUserService))
                            log.debug("Valid login for {}", email)
                        } else {
                            channel ! LoginResponse(msg, Left(LoginError("Invalid login")))
                            log.debug("Invalid login for {}", email)
                        }
                    case GetPartnerUserResponse(_, Left(e)) =>
                        channel ! LoginResponse(msg, Left(LoginError("Invalid login", e)))
                        log.error("Error getting PartnerUser from PartneruserService for {}", email)
                }

            case None =>
                log.debug("Cache miss for {}", email)
                (self ? CreatePartnerUserService(email)).onSuccess {
                    case CreatePartnerUserServiceResponse(_, Right(pus)) =>
                        log.debug("Seeded cache for {}", email)
                        login(msg, channel)
                    case CreatePartnerUserServiceResponse(_, Left(e)) =>
                        channel ! LoginResponse(msg, Left(LoginError("Invalid login", e)))
                        log.debug("Invalid login for {}", email)
                }
        }
    }


    def handle = {

        case msg @ CreatePartnerUserService(email) =>
            val channel = context.sender

            log.debug("Loading PartnerUser for {}", email)
            Option(partnerUserDao.findByEmail(email)).cata(
                pu => {
                    val pus = new PartnerUserServiceActorClient(context.actorOf(Props().withCreator {
                        val p = Option(partnerUserDao.findByEmail(email)).get
                        new PartnerUserService(p, partnerUserDao, partnerViewDao)
                    }))
                    cache(email) = pus
                    channel ! CreatePartnerUserServiceResponse(msg, Right(pus))
                },
                channel ! CreatePartnerUserServiceResponse(msg, Left(new PartnerUserException(
                    "No user with email %s" format email))))

        case msg @ CacheEntryRemoved(partnerUserId: String, pus: PartnerUserService, cause: String) =>
            log.debug("Received {}", msg)
            pus.logout(partnerUserId)
            for((e, s) <- cache.view if (s.id == pus.id)) {
                cache -= e
                log.debug("Removed {} from cache", pus.id)
            }
            log.debug("Sent logout for {}", pus)

        case msg: Login => login(msg, context.sender)
        case msg @ Logout(partnerUserId) =>
            val channel = context.sender

            try {
                log.debug("Processing {}", msg)
                cacheById.remove(partnerUserId).cata(
                    pus => {
                        channel ! LogoutResponse(msg, Right(true))
                        log.debug("Successfully logged out {}", partnerUserId)
                    },
                    {
                        channel ! LogoutResponse(msg, Right(false))
                        log.debug("Did not find PartnerUser {} to logout", partnerUserId)
                    })
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(PartnerUserException("Could not logout partner", e)))
                    log.error("Unexpected error processing %s" format msg, e)
            }

        case msg @ Locate(partnerUserId) =>
            context.sender ! LocateResponse(msg, cacheById.get(partnerUserId).toRight(LoginError("No partner user")))

    }

*/
}

case class PartnerUserId(id: String) extends Identifiable
case class Email(id: String) extends Identifiable
