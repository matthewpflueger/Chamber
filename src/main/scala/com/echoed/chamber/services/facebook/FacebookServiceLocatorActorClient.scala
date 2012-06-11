package com.echoed.chamber.services.facebook

import reflect.BeanProperty
import akka.actor.ActorRef
import com.echoed.chamber.services.ActorClient
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._


class FacebookServiceLocatorActorClient
        extends FacebookServiceLocator
        with ActorClient
        with Serializable {

    @BeanProperty var facebookServiceLocatorActor: ActorRef = _

    private implicit val timeout = Timeout(20 seconds)

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
