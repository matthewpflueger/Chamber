package com.echoed.chamber.services.facebook

import akka.actor.Actor
import com.echoed.chamber.domain.{FacebookFriend, FacebookUser}
import collection.mutable.WeakHashMap
import org.codehaus.jackson.`type`.TypeReference
import com.googlecode.batchfb.FacebookBatcher
import com.googlecode.batchfb.`type`.Paged
import reflect.BeanProperty
import java.util.Properties
import com.codahale.jerkson.ScalaModule
import org.slf4j.LoggerFactory


class FacebookAccessActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[FacebookAccessActor])

    @BeanProperty var clientId: String = null
    @BeanProperty var clientSecret: String = null
    @BeanProperty var redirectUrl: String = null

    @BeanProperty var properties: Properties = null

    private val cache = WeakHashMap[String, FacebookBatcher]()

    override def preStart {
        //NOTE: getting the properties like this is necessary due to a bug in Akka's Spring integration
        // where placeholder values were not being resolved
        {
            clientId = properties.getProperty("clientId")
            clientSecret = properties.getProperty("clientSecret")
            redirectUrl = properties.getProperty("redirectUrl")
            //clientId != null && clientSecret != null && redirectUrl != null
        } ensuring (clientId != null && clientSecret != null && redirectUrl != null, "Missing parameters")
    }

    def receive = {
        case ("accessToken", code: String) => {
            logger.debug("Requesting access token for code {}", code)
            self.channel ! FacebookBatcher.getAccessToken(clientId, clientSecret, code, redirectUrl)
            logger.debug("Got access token for code {}", code)
        }
        case ("me", accessToken: String) => {
            logger.debug("Requesting me with access token {}", accessToken)
            val me = getFacebookBatcher(accessToken).graph("me", new TypeReference[FacebookUser] {}).get
            me.accessToken = accessToken
            self.channel ! me
            logger.debug("Got me {}", me)
        }
        case ("friends", accessToken: String, facebookId: String) => {
            logger.debug("Requesting friends for {} with access token {}", facebookId, accessToken)
            val pagedFriends: Paged[FacebookFriend] = getFacebookBatcher(accessToken).graph(
                    ("%s/friends" format facebookId),
                    new TypeReference[Paged[FacebookFriend]] {}).get
            self.channel ! pagedFriends.getData
            logger.debug("Got friends for {}", facebookId)
        }
    }

    private def getFacebookBatcher(accessToken: String) = {
        cache.getOrElse(accessToken, {
            logger.debug("Cache miss for FacebookBatcher key {}", accessToken)
            val facebookBatcher = new FacebookBatcher(accessToken)
            facebookBatcher.getMapper.registerModule(new ScalaModule(Thread.currentThread().getContextClassLoader))
            cache += (accessToken -> facebookBatcher)
            facebookBatcher
        })
    }
}