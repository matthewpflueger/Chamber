package com.echoed.chamber.services.partneruser

import akka.actor.ActorRef
import reflect.BeanProperty

class PartnerUserServiceCreatorActorClient extends PartnerUserServiceCreator {

    @BeanProperty var partnerUserServiceCreatorActor: ActorRef = _


    def createPartnerUserService(email: String) =
        (partnerUserServiceCreatorActor ? CreatePartnerUserService(email)).mapTo[CreatePartnerUserServiceResponse]


}
