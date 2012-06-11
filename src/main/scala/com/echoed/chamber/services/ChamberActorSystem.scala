package com.echoed.chamber.services

import akka.actor.ActorSystem
import org.springframework.beans.factory.FactoryBean

class ChamberActorSystem extends FactoryBean[ActorSystem] {

    private var actorSystem: ActorSystem = _

    def getObjectType = classOf[ActorSystem]

    def isSingleton = true

    def getObject = {
        actorSystem = ActorSystem("Chamber")
        actorSystem
    }

    def destroy = actorSystem.shutdown()
}

