package com.echoed.chamber.services.twitter

import akka.actor.Actor
import com.echoed.chamber.domain.{TwitterFollower, TwitterUser}
import collection.mutable.WeakHashMap
import twitter4j.User
import org.codehaus.jackson.`type`.TypeReference
import reflect.BeanProperty
import java.util.Properties
import com.codahale.jerkson.ScalaModule
import org.slf4j.LoggerFactory
import twitter4j.auth.{RequestToken,AccessToken}
import twitter4j.{TwitterFactory, Twitter, TwitterException}
import twitter4j.conf.ConfigurationBuilder

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 11/7/11
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */

class TwitterAccessActor extends Actor{

  private final val logger = LoggerFactory.getLogger(classOf[TwitterAccessActor])

  @BeanProperty var consumerKey: String = null //Called Consumer Key for Twitter
  @BeanProperty var consumerSecret: String = null  //Called Consumer Secret for Twitter
  @BeanProperty var callbackUrl: String = null  //Callback Url

  @BeanProperty var properties: Properties = null

  private val cache = WeakHashMap[String, Twitter]()

    override def preStart {
        //NOTE: getting the properties like this is necessary due to a bug in Akka's Spring integration
        // where placeholder values were not being resolved
        {
            consumerKey = properties.getProperty("consumerKey")
            consumerSecret = properties.getProperty("consumerSecret")
            callbackUrl = properties.getProperty("callbackUrl")
            consumerKey != null && consumerSecret != null && callbackUrl != null
        } ensuring (_ == true, "Missing parameters")
    }

    def receive = {

        case ("requestToken") =>{
            val twitterHandler = getTwitterHandler(null)
            var requestToken: RequestToken = twitterHandler.getOAuthRequestToken("http://localhost:8080/twitter/login")
            val key: String = "requestToken:" + requestToken.getToken
            cache +=(key -> twitterHandler)
            self.channel ! requestToken
        }

        case ("accessToken",requestToken: RequestToken, oAuthVerifier: String) =>{
            var key = "requestToken:" + requestToken.getToken
            val twitterHandler = getTwitterHandler(key)
            var accessToken: AccessToken = twitterHandler.getOAuthAccessToken(requestToken,oAuthVerifier)
            self.channel ! accessToken
        }

        case ("accessToken", accessToken: String,  accessTokenSecret: String)=> {
            var key ="accessToken:" + accessToken
            val twitterHandler = getTwitterHandler(key,accessToken,accessTokenSecret)
            self.channel ! twitterHandler.getOAuthAccessToken()
        }

        case ("getUser",accessToken:String,  accessTokenSecret: String, userId: Long ) => {
            val twitterHandler = getTwitterHandler("accessToken:" + accessToken,accessToken,accessTokenSecret)
            val user: User = twitterHandler.showUser(userId)
            val twitterUser: TwitterUser = new TwitterUser(user.getId.toString, user.getScreenName,user.getName, user.getLocation, user.getTimeZone)
            self.channel ! twitterUser
        }

        case ("updateStatus", accessToken: String, accessTokenSecret: String,  status: String) => {
          val key="accessToken:" + accessToken
          val twitterHandler = getTwitterHandler(key,accessToken,accessTokenSecret)
          self.channel ! twitterHandler.updateStatus(status).toString()
        }
    }

    private def getTwitterHandler(key:String): Twitter = {
      getTwitterHandler(key,null,null)
    }

    private def getTwitterHandler(key:String,accessToken:String, accessTokenSecret:String)={

       cache.getOrElse(key,{
         var configurationBuilder: ConfigurationBuilder = new ConfigurationBuilder()

         configurationBuilder.setOAuthConsumerKey(consumerKey)
                             .setOAuthConsumerSecret(consumerSecret)
                             .setOAuthAccessToken(accessToken)
                             .setOAuthAccessTokenSecret(accessTokenSecret)

         val twitterHandler: Twitter = new TwitterFactory(configurationBuilder.build()).getInstance()
         twitterHandler
       })
    }
}