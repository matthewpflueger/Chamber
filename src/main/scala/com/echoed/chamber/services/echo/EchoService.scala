package com.echoed.chamber.services.echo

import akka.dispatch.Future
import com.echoed.chamber.domain._


trait EchoService {

    def recordEchoPossibility(echoPossibility: EchoPossibility): Future[EchoPossibility]

    def getEchoPossibility(echoPossibilityId: String): Future[EchoPossibility]

    def echo(echoRequestMessage: EchoRequestMessage): Future[EchoResponseMessage]

    def recordEchoClick(echoClick: EchoClick, postId: String): Future[(EchoClick, String)]

}
