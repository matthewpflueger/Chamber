package com.echoed.chamber.services.adminuser

import akka.util.Timeout
import com.echoed.chamber.services._
import akka.actor._
import scalaz._
import Scalaz._
import akka.actor.Terminated
import com.google.common.collect.HashMultimap
import com.echoed.chamber.domain.Identifiable
import scala.collection.JavaConversions._


class AdminUserServiceManager(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        adminUserServiceCreator: (ActorContext, Message) => ActorRef,
        implicit val timeout: Timeout = Timeout(20000)) extends EchoedService {

    private val active = HashMultimap.create[Identifiable, ActorRef]()

    def handle = {
        case Terminated(ref) => active.values.removeAll(active.values.filter(_ == ref))

        case RegisterAdminUserService(adminUser) =>
            active.put(AdminUserId(adminUser.id), context.sender)
            active.put(Email(adminUser.email), context.sender)

        case msg: EmailIdentifiable with AdminUserMessage =>
            active.get(Email(msg.email)).headOption.cata(
                _.forward(msg),
                {
                    val adminUserService = context.watch(adminUserServiceCreator(context, LoginWithEmail(msg.email, msg, Option(sender))))
                    adminUserService.forward(msg)
                    active.put(Email(msg.email), adminUserService)
                })

        case msg: AdminUserIdentifiable =>
            active.get(AdminUserId(msg.adminUserId)).headOption.cata(
                _.forward(msg),
                {
                    val adminUserService = context.watch(adminUserServiceCreator(context, LoginWithCredentials(msg.credentials)))
                    adminUserService.forward(msg)
                    active.put(AdminUserId(msg.adminUserId), adminUserService)
                })

    }

}

case class AdminUserId(id: String) extends Identifiable
case class Email(id: String) extends Identifiable


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

