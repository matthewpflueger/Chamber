package com.echoed.chamber.services.twitter

import akka.actor.ActorRef
import twitter4j.auth.{RequestToken, AccessToken}
import com.echoed.chamber.domain.{TwitterUser, TwitterStatus, TwitterFollower,Echo}


class TwitterServiceActorClient(twitterServiceActor: ActorRef) extends TwitterService {

    def getRequestToken() =
            (twitterServiceActor ? ("getRequestToken")).mapTo[RequestToken]

    def getAccessToken(oAuthVerifier:String) =
            (twitterServiceActor ? ("getAccessToken",oAuthVerifier)).mapTo[AccessToken]

    def getUser() =
            (twitterServiceActor? ("getUser")).mapTo[TwitterUser]

    def getTwitterUser =
            (twitterServiceActor ? ("getTwitterUser")).mapTo[TwitterUser]

    def getFollowers =
            (twitterServiceActor ? ("getFollowers")).mapTo[List[TwitterFollower]]

    def assignEchoedUserId(id: String) =
            (twitterServiceActor ? ("assignEchoedUserId",id)).mapTo[TwitterUser]

//    def updateStatus(status:String) =
//            (twitterServiceActor ? ("updateStatus",status)).mapTo[TwitterStatus]

//    def getStatus(statusId:String) =
//            (twitterServiceActor ? ("getStatus",statusId)).mapTo[TwitterStatus]

    def getRetweetIds(tweetId:String) =
            (twitterServiceActor ? ("getRetweetIds",tweetId)).mapTo[Array[Long]]

    def echo(echo:Echo,  message:String) = {
            (twitterServiceActor ? ("echo",echo,message)).mapTo[TwitterStatus]
    }
}
