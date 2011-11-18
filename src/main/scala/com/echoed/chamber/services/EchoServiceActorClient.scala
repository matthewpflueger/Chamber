package com.echoed.chamber.services

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.domain.{FacebookPost, Echo, EchoPossibility,TwitterStatus}


class EchoServiceActorClient extends EchoService {

    @BeanProperty var echoServiceActor: ActorRef = _

    def recordEchoPossibility(echoPossibility: EchoPossibility) =
            (echoServiceActor ? ("recordEchoPossibility", echoPossibility)).mapTo[EchoPossibility]

    def getEchoPossibility(echoPossibilityId: String) =
            (echoServiceActor ? ("echoPossibility", echoPossibilityId)).mapTo[EchoPossibility]

    def echo(echoedUserId: String, echoPossibilityId: String, message: String) =
            (echoServiceActor ? ("echo", echoedUserId, echoPossibilityId, message)).mapTo[(Echo, FacebookPost)]

}
