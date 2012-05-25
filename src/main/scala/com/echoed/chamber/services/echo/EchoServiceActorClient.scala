package com.echoed.chamber.services.echo

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.domain._
import com.echoed.chamber.services.ActorClient


class EchoServiceActorClient extends EchoService with ActorClient {

    @BeanProperty var echoServiceActor: ActorRef = _

    def recordEchoPossibility(echoPossibility: Echo) =
            (echoServiceActor ? RecordEchoPossibility(echoPossibility)).mapTo[RecordEchoPossibilityResponse]

    def getEchoPossibility(echoPossibilityId: String) =
            (echoServiceActor ? GetEchoPossibility(echoPossibilityId)).mapTo[GetEchoPossibilityResponse]

    def getEcho(echoPossibilityId: String) =
            (echoServiceActor ? GetEcho(echoPossibilityId)).mapTo[GetEchoResponse]
    
    def getEchoById(echoId: String) =
            (echoServiceActor ? GetEchoById(echoId)).mapTo[GetEchoByIdResponse]

    def getEchoByIdAndEchoedUserId(echoId: String, echoedUserId: String) =
            (echoServiceActor ? GetEchoByIdAndEchoedUserId(echoId, echoedUserId)).mapTo[GetEchoByIdAndEchoedUserIdResponse]

    def recordEchoClick(echoClick: EchoClick, linkId: String, postId: String) =
            (echoServiceActor ? RecordEchoClick(echoClick, linkId, postId)).mapTo[RecordEchoClickResponse]

    def actorRef = echoServiceActor
}
