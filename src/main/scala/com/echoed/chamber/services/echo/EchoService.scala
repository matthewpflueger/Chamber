package com.echoed.chamber.services.echo

import akka.dispatch.Future
import com.echoed.chamber.domain._


trait EchoService {

    def recordEchoPossibility(echoPossibility: EchoPossibility): Future[RecordEchoPossibilityResponse]

    def getEchoPossibility(echoPossibilityId: String): Future[GetEchoPossibilityResponse]

    def getEcho(echoPossibilityId:String): Future[GetEchoResponse]

    def recordEchoClick(echoClick: EchoClick, postId: String): Future[RecordEchoClickResponse]

}
