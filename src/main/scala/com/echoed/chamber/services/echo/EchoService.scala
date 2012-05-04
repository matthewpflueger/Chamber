package com.echoed.chamber.services.echo

import akka.dispatch.Future
import com.echoed.chamber.domain._


trait EchoService {

    def recordEchoPossibility(echoPossibility: Echo): Future[RecordEchoPossibilityResponse]

    def getEchoPossibility(echoPossibilityId: String): Future[GetEchoPossibilityResponse]

    def getEcho(echoPossibilityId:String): Future[GetEchoResponse]

    def getEchoById(echoId: String): Future[GetEchoByIdResponse]

    def recordEchoClick(echoClick: EchoClick, linkId: String, postId: String): Future[RecordEchoClickResponse]

}
