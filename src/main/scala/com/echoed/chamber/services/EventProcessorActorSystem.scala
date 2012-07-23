package com.echoed.chamber.services

import akka.actor.{ActorSystem, ActorRef}

class EventProcessorActorSystem(actorSystem: ActorSystem) extends EventProcessor {
    type Subscriber = ActorRef

    def subscribe(subscriber: ActorRef, to: Class[_ <: Event]) = actorSystem.eventStream.subscribe(subscriber, to)

    def unsubscribe(subscriber: ActorRef, from: Class[_ <: Event]) = actorSystem.eventStream.subscribe(subscriber, from)

    def unsubscribe(subscriber: ActorRef) {
        actorSystem.eventStream.unsubscribe(subscriber)
    }

    def publish(event: Event) {
        actorSystem.eventStream.publish(event)
    }
}
