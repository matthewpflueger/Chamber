package com.echoed.chamber.services.facebook

import reflect.BeanProperty
import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient


class FacebookServiceLocatorActorClient
        extends FacebookServiceLocator
        with ActorClient
        with Serializable {

    @BeanProperty var facebookServiceLocatorActor: ActorRef = _

    def locateByCode(code: String, queryString: String) =
            (facebookServiceLocatorActor ? LocateByCode(code, queryString)).mapTo[LocateByCodeResponse]

    def locateById(facebookUserId: String) =
            (facebookServiceLocatorActor ? LocateById(facebookUserId)).mapTo[LocateByIdResponse]

    def actorRef = facebookServiceLocatorActor

    def logout(facebookUserId: String) =
            (facebookServiceLocatorActor ? Logout(facebookUserId)).mapTo[LogoutResponse]

    def locateByFacebookId(facebookId: String, accessToken: String) =
            (facebookServiceLocatorActor ? LocateByFacebookId(facebookId, accessToken)).mapTo[LocateByFacebookIdResponse]
}
