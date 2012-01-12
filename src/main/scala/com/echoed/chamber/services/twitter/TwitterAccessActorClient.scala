package com.echoed.chamber.services.twitter

import com.echoed.chamber.domain.TwitterStatus
import reflect.BeanProperty
import akka.actor.ActorRef
import twitter4j.auth.RequestToken
import com.echoed.chamber.services.ActorClient

class TwitterAccessActorClient extends TwitterAccess with ActorClient {

    @BeanProperty var twitterAccessActor: ActorRef = _

    def getRequestToken(callbackUrl: String) =
            (twitterAccessActor ? FetchRequestToken(callbackUrl)).mapTo[FetchRequestTokenResponse] //("requestToken", callbackUrl)).mapTo[RequestToken]

    def getAccessToken(requestToken: RequestToken, oAuthVerifier: String) =
            (twitterAccessActor ? GetAccessTokenForRequestToken(requestToken, oAuthVerifier)).mapTo[GetAccessTokenForRequestTokenResponse] //("accessToken", requestToken, oAuthVerifier)).mapTo[AccessToken]

    def getAccessToken(accessToken: String, accessTokenSecret: String) =
            (twitterAccessActor ? FetchAccessToken(accessToken, accessTokenSecret)).mapTo[FetchAccessTokenResponse] //("accessToken", accessToken, accessTokenSecret)).mapTo[AccessToken]

    def getUser(accessToken: String, accessTokenSecret: String, userId: Long) =
            (twitterAccessActor ? FetchUser(accessToken, accessTokenSecret, userId)).mapTo[FetchUserResponse] //("getUser", accessToken, accessTokenSecret, userId)).mapTo[TwitterUser]

    def getFollowers(accessToken: String, accessTokenSecret: String, twitterUserId: String, twitterId: Long) =
            (twitterAccessActor ? FetchFollowers(accessToken, accessTokenSecret, twitterUserId, twitterId)).mapTo[FetchFollowersResponse] //("getFollowers", accessToken, accessTokenSecret, twitterUserId, twitterId)).mapTo[List[TwitterFollower]]

    def updateStatus(accessToken: String, accessTokenSecret: String, status: TwitterStatus) =
            (twitterAccessActor ? UpdateStatus(accessToken, accessTokenSecret, status)).mapTo[UpdateStatusResponse] //("updateStatus", accessToken, accessTokenSecret, status)).mapTo[TwitterStatus]

    def actorRef = twitterAccessActor
}
