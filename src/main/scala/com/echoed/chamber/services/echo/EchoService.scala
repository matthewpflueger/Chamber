package com.echoed.chamber.services.echo

import akka.dispatch.Future
import com.echoed.chamber.domain._


trait EchoService {

    def recordEchoPossibility(echoPossibility: EchoPossibility): Future[RecordEchoPossibilityResponse]

    def getEchoPossibility(echoPossibilityId: String): Future[EchoPossibility]

    def getEcho(echoPossibilityId:String): Future[(Echo,String)]

    def recordEchoClick(echoClick: EchoClick, postId: String): Future[(EchoClick, String)]

}
