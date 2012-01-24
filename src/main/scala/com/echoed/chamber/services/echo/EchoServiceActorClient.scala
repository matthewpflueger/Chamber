package com.echoed.chamber.services.echo

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.domain._


class EchoServiceActorClient extends EchoService {

    @BeanProperty var echoServiceActor: ActorRef = _

    def recordEchoPossibility(echoPossibility: EchoPossibility) =
            (echoServiceActor ? RecordEchoPossibility(echoPossibility)).mapTo[RecordEchoPossibilityResponse]

    def getEchoPossibility(echoPossibilityId: String) =
            (echoServiceActor ? GetEchoPossibility(echoPossibilityId)).mapTo[GetEchoPossibilityResponse]

    def getEcho(echoPossibilityId: String) =
            (echoServiceActor ? GetEcho(echoPossibilityId)).mapTo[GetEchoResponse]

    def recordEchoClick(echoClick: EchoClick, postId: String) =
            (echoServiceActor ? RecordEchoClick(echoClick, postId)).mapTo[RecordEchoClickResponse]


}
