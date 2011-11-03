package com.echoed.chamber.services.facebook

import akka.dispatch.Future
import com.echoed.chamber.domain.{FacebookFriend, FacebookUser}
import reflect.BeanProperty
import akka.actor.{Actor, ActorRef}


class FacebookAccessActorClient extends FacebookAccess {

    @BeanProperty var facebookAccessActor: ActorRef = null


    def getAccessToken(code: String) = {
        Future[String] {
            (facebookAccessActor ? ("accessToken", code)).get.asInstanceOf[String]
        }
    }

    def getMe(accessToken: String) = {
        Future[FacebookUser] {
            (facebookAccessActor ? ("me", accessToken)).get.asInstanceOf[FacebookUser]
        }
    }

    def getFriends(accessToken: String) = {
        Future[List[FacebookFriend]] {
            (facebookAccessActor ? ("friends", accessToken)).get.asInstanceOf[List[FacebookFriend]]
        }
    }
}

