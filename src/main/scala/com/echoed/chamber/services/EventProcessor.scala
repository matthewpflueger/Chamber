package com.echoed.chamber.services


trait EventProcessor {
    type Subscriber

    def subscribe(subscriber: Subscriber, to: Class[_ <: Event]): Boolean

    def unsubscribe(subscriber: Subscriber, from: Class[_ <: Event]): Boolean

    def unsubscribe(subscriber: Subscriber): Unit

    def publish(event: Event): Unit

}

trait Event extends Serializable
