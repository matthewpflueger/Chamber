package com.echoed.chamber.services

import akka.actor.ActorRef


trait ActorClient {

    def actorRef: ActorRef

}
