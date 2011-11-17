package com.echoed.chamber.services.facebook

import akka.dispatch.Future
import reflect.BeanProperty
import akka.actor.{Actor, ActorRef}
import com.echoed.chamber.domain.{FacebookPost, FacebookFriend, FacebookUser}


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

    def post(accessToken: String, facebookId: String, facebookPost: FacebookPost) =
        (facebookAccessActor ? ("post", accessToken, facebookId, facebookPost)).mapTo[FacebookPost]

}

