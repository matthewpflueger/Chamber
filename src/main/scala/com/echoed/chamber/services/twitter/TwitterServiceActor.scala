package com.echoed.chamber.services.twitter

import com.echoed.chamber.domain.TwitterUser
import com.echoed.chamber.dao.TwitterUserDao
import akka.actor.Actor
import twitter4j.auth.{RequestToken, AccessToken}

class TwitterServiceActor(twitterAccess: TwitterAccess,
                          twitterUserDao: TwitterUserDao,
                          requestToken: RequestToken,
                          twitterUser: TwitterUser) extends Actor {

      def this(twitterAccess:TwitterAccess,twitterUserDao:TwitterUserDao,requestToken:RequestToken) = this(twitterAccess,twitterUserDao,requestToken,null)
      def this(twitterAccess:TwitterAccess,twitterUserDao:TwitterUserDao,twitterUser:TwitterUser) = this(twitterAccess,twitterUserDao,null, twitterUser)



      def receive = {
        case ("getRequestToken") =>{
          self.channel ! this.requestToken
        }

        case ("getAccessToken",oAuthVerifier:String) =>{
          var accessToken: AccessToken = twitterAccess.getAccessToken(requestToken,oAuthVerifier).get.asInstanceOf[AccessToken]
          self.channel ! accessToken
        }

        case("getUser") =>{
          self.channel ! twitterAccess.getUser(twitterUser.accessToken,twitterUser.accessTokenSecret,twitterUser.id.toLong).get.asInstanceOf[TwitterUser]
        }

        case("getTwitterUser") =>{
          self.channel ! twitterUser
        }

        case("assignEchoedUserId", echoedUserId:String) =>{
          twitterUser.echoedUserId = echoedUserId
          twitterUserDao.insertOrUpdateTwitterUser(twitterUser)
          self.channel ! twitterUser
        }
        case ("updateStatus", status:String) => {
          self.channel ! twitterAccess.updateStatus(twitterUser.getAccessToken(),twitterUser.getAccessTokenSecret(),status)
        }
      }
}