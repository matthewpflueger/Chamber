package com.echoed.chamber.services.facebook

import akka.actor.ActorRef
import reflect.BeanProperty
import com.echoed.chamber.services.ActorClient


class FacebookServiceCreatorActorClient
        extends FacebookServiceCreator
        with ActorClient
        with Serializable {

    @BeanProperty var facebookServiceCreatorActor: ActorRef = _

    def createFromCode(code: String, queryString: String) =
            (facebookServiceCreatorActor ? CreateFromCode(code, queryString)).mapTo[CreateFromCodeResponse]

    def createFromId(facebookUserId: String) =
            (facebookServiceCreatorActor ? CreateFromId(facebookUserId)).mapTo[CreateFromIdResponse]

    def actorRef = facebookServiceCreatorActor

    def createFromFacebookId(facebookId: String, accessToken: String) =
            (facebookServiceCreatorActor ? CreateFromFacebookId(facebookId, accessToken)).mapTo[CreateFromFacebookIdResponse]
}
