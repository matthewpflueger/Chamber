package com.echoed.chamber.services.adminuser

import akka.util.Timeout
import com.echoed.chamber.services._
import akka.actor._
import akka.pattern._
import scalaz._
import Scalaz._
import scala.Left
import scala.Right
import scala.Some
import com.echoed.chamber.services.state.ReadAdminUserServiceManagerState
import akka.actor.Terminated
import com.echoed.chamber.services.state.ReadAdminUserServiceManagerStateResponse
import com.google.common.collect.HashBiMap

class AdminUserServiceManager(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        implicit val timeout: Timeout = Timeout(20000)) extends OnlineOfflineService {

    private var emailIds: Map[String, String] = _
    private val active = HashBiMap.create[String, ActorRef]()

    override def preStart() {
        super.preStart()
        mp(ReadAdminUserServiceManagerState()).pipeTo(context.self)
    }

    override def lifespan = Unit

    def init = {
        case ReadAdminUserServiceManagerStateResponse(_, Right(state)) =>
            emailIds = state
            becomeOnline
    }

    def online = {
        case CreateAdminUserService(msg @ CreateAdminUser(aucc, adminUser), sender) =>
            emailIds.get(adminUser.email).cata(
                _ => sender ! CreateAdminUserResponse(msg, Left(AdminUserException("Email already exists"))),
                {
                    val ref = context.watch(context.actorOf(Props(new AdminUserService(mp, ep)), adminUser.id))
                    ref.tell(msg, sender)
                    active.put(adminUser.email, ref)
                    emailIds += (adminUser.email -> adminUser.id)
                })

        case msg: AdminUserIdentifiable => context.actorFor(msg.adminUserId).forward(msg)

        case msg @ Login(email, password) =>
            Option(active.get(email)).cata(
                _.forward(msg),
                emailIds.get(email).cata(
                    id => {
                        val ref = context.watch(context.actorOf(Props(new AdminUserService(mp, ep, Some(id))), id))
                        ref.forward(msg)
                        active.put(email, ref)
                    },
                    sender ! LoginResponse(msg, Left(LoginError("Invalid login")))))

        case Terminated(ref) => active.inverse().remove(ref) //active = active.filter(ref != _._2)
    }


/*
    private val cache: ConcurrentMap[String, ActorRef] = new ConcurrentHashMap[String, ActorRef]()
    private var cacheById = cacheManager.getCache[ActorRef]("AdminUserServices", Some(new CacheListenerActorClient(self)))

    def login(msg: Login, channel: ActorRef) {
        val email = msg.email
        val password = msg.password
        log.debug("Locating AdminUserService for {}", email)
        cache.get(email) match {
            case Some(adminUserService) =>
                log.debug("Cache hit for {}", email)
                (adminUserService ? GetAdminUser(null)).onSuccess {
//                adminUserService.getAdminUser(null).onSuccess {
                    case GetAdminUserResponse(_, Right(au)) =>
                        if (au.isPassword(password)) {
                            cacheById.put(au.id, adminUserService)
                            channel ! LoginResponse(msg, Right(au))
                            log.debug("Valid login for {}", email)
                        } else {
                            channel ! LoginResponse(msg, Left(LoginError("Invalid login")))
                            log.debug("Invalid login for {}", email)
                        }
                    case GetAdminUserResponse(_, Left(e)) =>
                        channel ! LoginResponse(msg, Left(LoginError("Invalid login", e)))
                        log.error("Error getting PartnerUser from PartneruserService for {}", email)
                }

            case None =>
                log.debug("Cache miss for {}", email)
                (self ? CreateAdminUserService(email)).onSuccess {
                    case CreateAdminUserServiceResponse(_, Right(aus)) =>
                        cache(email) = aus
                        log.debug("Seeded cache for {}", email)
                        login(msg, channel)
                    case CreateAdminUserServiceResponse(_, Left(e)) =>
                        channel ! LoginResponse(msg, Left(LoginError("Invalid login", e)))
                        log.debug("Invalid login for {}", email)
                }
        }
    }

    def handle = {
        case msg @ CreateAdminUserService(email) =>
            val channel = context.sender

            log.debug("Loading AdminUser for {}", email)
            Option(adminUserDao.findByEmail(email)).cata(
                au => {
                    val aus = /*new AdminUserServiceActorClient(*/context.actorOf(Props().withCreator {
                        val a = Option(adminUserDao.findByEmail(email)).get
                        new AdminUserServiceActor(a, adminUserDao, adminViewDao, partnerSettingsDao, partnerDao)
                    })//)
                    cache(email) = aus
                    channel ! CreateAdminUserServiceResponse(msg, Right(aus))
                },
                channel ! CreateAdminUserServiceResponse(msg, Left(new AdminUserException(
                    "No user with email %s" format email))))

        case msg @ CreateAdminUser(email, name, password) =>
            val channel = context.sender

            log.debug("Creating Admin User: {}:{}", name, email)
            var adminUser = new AdminUser(name, email)
            adminUser = adminUser.createPassword(password)
            log.debug("AdminUser: {} ", adminUser)
            adminUserDao.insert(adminUser)
            channel ! CreateAdminUserResponse(msg, Right(adminUserDao.findByEmail(email)))

        case msg @ CacheEntryRemoved(adminUserId: String, aus: ActorRef, cause: String) =>
            log.debug("Received {}", msg)
            aus ! Logout(adminUserId) //.logout(adminUserId)
//            for((e, s) <- cache.view if (s.id == adminUserId)) {
//                cache -= e
//                log.debug("Removed {} from cache", adminUserId)
//            }
            log.debug("Sent logout for {}", aus)

        case msg: Login => login(msg, context.sender)

        case msg @ Logout(adminUserId) =>
            val channel = context.sender

            try {
                log.debug("Processing {}", msg)
                cacheById.remove(adminUserId).cata(
                pus => {
                    channel ! LogoutResponse(msg, Right(true))
                    log.debug("Successfully logged out {}", adminUserId)
                },
                {
                    channel ! LogoutResponse(msg, Right(false))
                    log.debug("Did not find PartnerUser {} to logout", adminUserId)
                })
            } catch {
                case e =>
                    channel ! LogoutResponse(msg, Left(AdminUserException("Could not logout partner", e)))
                    log.error("Unexpected error processing %s" format msg, e)
            }

        case msg @ LocateAdminUserService(adminUserId) =>
            context.sender ! LocateAdminUserServiceResponse(msg, cacheById.get(adminUserId).toRight(LoginError("No partner user")))

    }
*/
}


object ActorErrorTest extends App {

    val sys = ActorSystem("test")

    sys.eventStream.subscribe(sys.actorOf(Props(new Actor {
        def receive = {
            case event => sys.log.error("Received event {}", event)
        }
    })), classOf[Object])

    val ref = sys.actorOf(Props(new Actor {
        sys.log.error("Sleeping")
        Thread.sleep(1000)
        sys.log.error("Awake")

        sys.log.error("Throwing error in first actor")
        throw new Exception("Error in constructor of first actor")

        def receive = {
            case 'throw_constructor => context.watch(context.actorOf(Props(new Actor {
                sys.log.error("Throwing exception in constructor")
                throw new Exception("Exception in constructor!")
                def receive = {
                    case m => sys.log.debug("Received message {} in throw exception in constructor actor", m)
                }
            }), "throw_constructor"))

            case m @ Terminated(actorRef) => sys.log.error("Received Terminated message {}", m)
        }
    }))

    sys.log.error("Did we get here before awake?")
    sys.log.error("Telling it to throw error")
    ref.tell('throw_constructor)
    Thread.sleep(10000)
    sys.log.error("Shutting down")
    sys.shutdown()
}


object ReceiveTimeoutTest extends App {


    val sys = ActorSystem("test")

//    sys.eventStream.subscribe(sys.actorOf(Props(new Actor {
//        def receive = {
//            case event => sys.log.error("Received event {}", event)
//        }
//    })), classOf[Object])

    val ref = sys.actorOf(Props(new EchoedService {

//        context.receiveTimeout
        import akka.util.duration._
        context.setReceiveTimeout(2 seconds)

//        def init = {
//            case ReceiveTimeout =>
//                sys.log.error("Got ReceiveTimeout {}", context.receiveTimeout)
//        }

        def handle = {
            case 'hello => sys.log.error("Got hello")

            case ReceiveTimeout =>
                sys.log.error("Got ReceiveTimeout {}", context.receiveTimeout)
        }
    }))

    sys.log.error("Sleeping - should receive timeout")
    Thread.sleep(4000)
    sys.log.error("Awake, sending hello")
    ref.tell('hello)
    sys.log.error("Sleeping again - do we receive another timeout?")
    Thread.sleep(4000)

    sys.log.error("Shutting down")
    sys.shutdown()
}

