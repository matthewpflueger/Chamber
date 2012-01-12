package com.echoed.chamber.services.twitter

import akka.actor.ActorRef
import twitter4j.auth.AccessToken
import com.echoed.chamber.domain.Echo
import com.echoed.chamber.services.ActorClient


class TwitterServiceActorClient(twitterServiceActor: ActorRef) extends TwitterService with ActorClient {


    def getRequestToken =
            (twitterServiceActor ? GetRequestToken()).mapTo[GetRequestTokenResponse]

    def getAccessToken(oAuthVerifier: String) =
            (twitterServiceActor ? GetAccessToken(oAuthVerifier)).mapTo[GetAccessTokenResponse]

    def getUser =
            (twitterServiceActor ? GetUser()).mapTo[GetUserResponse]

    def getFollowers =
            (twitterServiceActor ? GetFollowers()).mapTo[GetFollowersResponse]

    def assignEchoedUser(echoedUserId: String) =
            (twitterServiceActor ? AssignEchoedUser(echoedUserId)).mapTo[AssignEchoedUserResponse]

    def tweet(echo:Echo,  message:String) =
            (twitterServiceActor ? Tweet(echo, message)).mapTo[TweetResponse]

    def actorRef = twitterServiceActor
}
