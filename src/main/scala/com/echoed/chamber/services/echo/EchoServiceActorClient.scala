package com.echoed.chamber.services.echo

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.domain._
import com.echoed.chamber.services.ActorClient
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._


class EchoServiceActorClient extends EchoService with ActorClient {

    @BeanProperty var echoServiceActor: ActorRef = _

    private implicit val timeout = Timeout(20 seconds)

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
