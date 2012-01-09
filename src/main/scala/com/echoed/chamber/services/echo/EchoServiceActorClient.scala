package com.echoed.chamber.services.echo

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.domain._


class EchoServiceActorClient extends EchoService {

    @BeanProperty var echoServiceActor: ActorRef = _

    def recordEchoPossibility(echoPossibility: EchoPossibility) =
            (echoServiceActor ? ("recordEchoPossibility", echoPossibility)).mapTo[EchoPossibility]

    def getEchoPossibility(echoPossibilityId: String) =
            (echoServiceActor ? ("echoPossibility", echoPossibilityId)).mapTo[EchoPossibility]

    def getEcho(echoPossibilityId: String) =
            (echoServiceActor ? ("getEcho",echoPossibilityId)).mapTo[(Echo,String)]

    def recordEchoClick(echoClick: EchoClick, postId: String) =
            (echoServiceActor ? ("recordEchoClick", echoClick, postId)).mapTo[(EchoClick, String)]


}
