package com.echoed.chamber.services.facebook

import akka.actor.Actor
import collection.mutable.WeakHashMap
import org.codehaus.jackson.`type`.TypeReference
import com.googlecode.batchfb.`type`.Paged
import reflect.BeanProperty
import java.util.Properties
import com.codahale.jerkson.ScalaModule
import org.slf4j.LoggerFactory
import com.echoed.chamber.domain.{FacebookPost, FacebookFriend, FacebookUser}
import com.googlecode.batchfb.{Param, FacebookBatcher}
import scala.collection.JavaConversions._


class FacebookAccessActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[FacebookAccessActor])

    @BeanProperty var clientId: String = null
    @BeanProperty var clientSecret: String = null
    @BeanProperty var redirectUrl: String = null

    @BeanProperty var properties: Properties = null

    private val cache = WeakHashMap[String, FacebookBatcher]()

    override def preStart {
        //NOTE: getting the properties like this is necessary due to a bug in Akka's Spring integration
        //where placeholder values were not being resolved
        {
            clientId = properties.getProperty("clientId")
            clientSecret = properties.getProperty("clientSecret")
            redirectUrl = properties.getProperty("redirectUrl")
            clientId != null && clientSecret != null && redirectUrl != null
        } ensuring (_ == true, "Missing parameters")
    }

    def receive = {
        case ("accessToken", code: String, queryString: String) => {
            logger.debug("Requesting access token for code {}", code)
            self.channel ! FacebookBatcher.getAccessToken(clientId, clientSecret, code, redirectUrl + queryString)
            logger.debug("Got access token for code {}", code)
        }
        case ("me", accessToken: String) => {
            logger.debug("Requesting me with access token {}", accessToken)
            val facebookUser = getFacebookBatcher(accessToken)
                    .graph("me", new TypeReference[Me] {})
                    .get
                    .createFacebookUser(accessToken)
            self.channel ! facebookUser
            logger.debug("Got me {}", facebookUser)
        }
        case ("friends", accessToken: String, facebookId: String, facebookUserId: String) => {
            logger.debug("Requesting friends for {} with access token {}", facebookId, accessToken)
            val pagedFriends = getFacebookBatcher(accessToken).graph(
                    ("%s/friends" format facebookId),
                    new TypeReference[Paged[Friend]] {}).get.getData
            logger.debug("Found {} friends for FacebookUser {}", pagedFriends.size(), facebookUserId)
            val facebookFriends = asScalaBuffer(pagedFriends).map(_.createFacebookFriend(facebookUserId)).toList
            self.channel ! facebookFriends
            logger.debug("Sent {} friends for FacebookUser {}", facebookFriends.length, facebookUserId)
        }
        case ("post", accessToken: String, facebookId: String, facebookPost: FacebookPost) => {
            logger.debug("Creating new post for {} with access token {}", facebookId, accessToken)
            val result = getFacebookBatcher(accessToken).post(
                ("%s/feed" format facebookId),
                new Param("message", facebookPost.message),
                new Param("picture", facebookPost.picture),
                new Param("link", facebookPost.link)).get()
            val fp = facebookPost.copy(facebookId = result)
            self.channel ! fp
            logger.debug("Successfully posted {}", fp)
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
