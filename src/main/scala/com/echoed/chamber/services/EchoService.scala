package com.echoed.chamber.services

import com.echoed.chamber.domain.EchoPossibility
import akka.dispatch.Future


trait EchoService {

    def recordEchoPossibility(echoPossibility: EchoPossibility): Future[EchoPossibility]

}