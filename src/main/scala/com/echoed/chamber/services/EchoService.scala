package com.echoed.chamber.services

import akka.dispatch.Future
import com.echoed.chamber.domain._


trait EchoService {

    def recordEchoPossibility(echoPossibility: EchoPossibility): Future[EchoPossibility]

    def getEchoPossibility(echoPossibilityId: String): Future[EchoPossibility]

    def echo(echoedUserId: String, echoPossibilityId: String, message: String): Future[(Echo, FacebookPost)]

    def recordEchoClick(echoClick: EchoClick, postId: String): Future[(EchoClick, String)]

}
