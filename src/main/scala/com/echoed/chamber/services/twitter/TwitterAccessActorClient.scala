package com.echoed.chamber.services.twitter

import com.echoed.chamber.domain.TwitterStatus
import reflect.BeanProperty
import akka.actor.ActorRef
import twitter4j.auth.RequestToken
import com.echoed.chamber.services.ActorClient
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._

class TwitterAccessActorClient extends TwitterAccess with ActorClient with Serializable {

    @BeanProperty var twitterAccessActor: ActorRef = _

    private implicit val timeout = Timeout(20 seconds)

    def getRequestToken(callbackUrl: String) =
            (twitterAccessActor ? FetchRequestToken(callbackUrl)).mapTo[FetchRequestTokenResponse]

    def getAccessToken(requestToken: RequestToken, oAuthVerifier: String) =
            (twitterAccessActor ? GetAccessTokenForRequestToken(requestToken, oAuthVerifier)).mapTo[GetAccessTokenForRequestTokenResponse]

    def getAccessToken(accessToken: String, accessTokenSecret: String) =
            (twitterAccessActor ? FetchAccessToken(accessToken, accessTokenSecret)).mapTo[FetchAccessTokenResponse]

    def getUser(accessToken: String, accessTokenSecret: String, userId: Long) =
            (twitterAccessActor ? FetchUser(accessToken, accessTokenSecret, userId)).mapTo[FetchUserResponse]

    def getFollowers(accessToken: String, accessTokenSecret: String, twitterUserId: String, twitterId: Long) =
            (twitterAccessActor ? FetchFollowers(accessToken, accessTokenSecret, twitterUserId, twitterId)).mapTo[FetchFollowersResponse]

    def updateStatus(accessToken: String, accessTokenSecret: String, status: TwitterStatus) =
            (twitterAccessActor ? UpdateStatus(accessToken, accessTokenSecret, status)).mapTo[UpdateStatusResponse]

    def actorRef = twitterAccessActor

    def logout(accessToken: String) =
            (twitterAccessActor ? Logout(accessToken)).mapTo[LogoutResponse]
}
