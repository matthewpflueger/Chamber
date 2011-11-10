package com.echoed.chamber.services.twitter

import com.echoed.chamber.domain.TwitterUser
import com.echoed.chamber.dao.TwitterUserDao
import akka.actor.Actor
import twitter4j.auth.{RequestToken, AccessToken}

class TwitterServiceActor(twitterUser:TwitterUser,
                          requestToken: RequestToken,
                          twitterAccess: TwitterAccess,
                          twitterUserDao: TwitterUserDao) extends Actor {

      def receive = {
        case ("getRequestToken") =>{
          self.channel ! this.requestToken
        }

        case ("getAccessToken",oAuthVerifier:String) =>{
          var accessToken: AccessToken = twitterAccess.getAccessToken(requestToken,oAuthVerifier).get.asInstanceOf[AccessToken]
          self.channel ! accessToken
        }

        case("getMe",accessToken: String, accessTokenSecret:String) =>{
          self.channel ! twitterAccess.getMe(accessToken,accessTokenSecret,twitterUser.id.toLong).get.asInstanceOf[TwitterUser]
        }

        case ("updateStatus", accessToken:String, accessTokenSecret:String,  status:String) =>{
          self.channel ! twitterAccess.updateStatus(accessToken,accessTokenSecret,status).get.asInstanceOf[String]
        }
      }
}