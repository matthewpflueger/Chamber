package com.echoed.chamber.services


trait Event extends Serializable

trait CreatedEvent extends Event

trait UpdatedEvent extends Event

trait DeletedEvent extends Event


trait EventProcessor {
    type Subscriber

    def subscribe(subscriber: Subscriber, to: Class[_ <: Event]): Boolean

    def unsubscribe(subscriber: Subscriber, from: Class[_ <: Event]): Boolean

    def unsubscribe(subscriber: Subscriber): Unit

    def publish(event: Event): Unit

    def apply(event: Event): Unit
}


