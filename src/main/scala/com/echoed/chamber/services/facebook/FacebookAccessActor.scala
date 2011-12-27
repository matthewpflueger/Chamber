package com.echoed.chamber.services.facebook

import akka.actor.Actor
import org.codehaus.jackson.`type`.TypeReference
import com.googlecode.batchfb.`type`.Paged
import reflect.BeanProperty
import com.codahale.jerkson.ScalaModule
import org.slf4j.LoggerFactory
import com.googlecode.batchfb.{Param, FacebookBatcher}
import com.googlecode.batchfb.err.{FacebookException => BFE}
import scala.collection.JavaConversions._
import com.echoed.chamber.domain._
import scalaz._
import Scalaz._
import java.net.URL
import scala.collection.mutable.WeakHashMap
import com.echoed.util.ScalaObjectMapper
import java.util.{Date, Properties}


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
            if (clientId == null) clientId = properties.getProperty("clientId")
            if (clientSecret == null) clientSecret = properties.getProperty("clientSecret")
            if (redirectUrl == null) redirectUrl = properties.getProperty("redirectUrl")
            clientId != null && clientSecret != null && redirectUrl != null
        } ensuring (_ == true, "Missing parameters")
    }

    def receive = {
        case ("accessToken", code: String, queryString: String) => {
            logger.debug("Requesting access token for code {}", code)
            logger.debug("Redirect Url: {}",redirectUrl + queryString)
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
        case msg @ GetFriends(accessToken, facebookId, facebookUserId) =>
            try {
                logger.debug("Requesting friends for {} with access token {}", facebookId, accessToken)
                val pagedFriends = getFacebookBatcher(accessToken).graph(
                        ("%s/friends" format facebookId),
                        new TypeReference[Paged[Friend]] {}).get.getData
                logger.debug("Found {} friends for FacebookUser {}", pagedFriends.size(), facebookUserId)
                val facebookFriends = asScalaBuffer(pagedFriends).map(_.createFacebookFriend(facebookUserId)).toList
                self.channel ! GetFriendsResponse(
                        msg,
                        Right(facebookFriends))
                logger.debug("Sent {} friends for FacebookUser {}", facebookFriends.length, facebookUserId)
            } catch {
                case e: BFE =>
                    logger.error("Error fetching friends for {}", facebookUserId, e)
                    self.channel ! GetFriendsResponse(
                        msg,
                        Left(FacebookException("Error fetching friends for %s" format facebookUserId, e)))
            }

        case ("post", accessToken: String, facebookId: String, facebookPost: FacebookPost) => {
            logger.debug("Creating new post for {} with access token {}", facebookId, accessToken)
            val result = getFacebookBatcher(accessToken).post(
                ("%s/feed" format facebookId),
                new Param("name", facebookPost.message),
                new Param("message", facebookPost.message),
                new Param("picture", facebookPost.picture),
                new Param("link", facebookPost.link),
                new Param("caption", facebookPost.message)).get()
            val fp = facebookPost.copy(facebookId = result)
            self.channel ! fp
            logger.debug("Successfully posted {}", fp)
        }
        case msg @ GetPostData(facebookPostData) =>
            try {
                val facebookId = facebookPostData.facebookPost.facebookId
                val accessToken = facebookPostData.facebookUser.accessToken

                logger.debug("Retrieving data for FacebookPost {}", facebookId)
                val url = new URL("http://graph.facebook.com/%s?accessToken=%s" format(facebookId, accessToken))
                val connection = url.openConnection()
                connection.setConnectTimeout(5000)
                connection.setReadTimeout(5000)
                val resultOption = Option(new ScalaObjectMapper().readValue(connection.getInputStream, classOf[PostData]))

                //TODO this does not work and I have no idea why - always throws an exception
//                val resultOption = Option(getFacebookBatcher(accessToken)
//                        .graph("%s" format facebookId, classOf[PostData])
//                        .get)


                resultOption.cata({ result =>
                    val likes = result.likes.data
                    val comments = result.comments.data
                    logger.debug("Received {} likes for {}", likes.length, result.id)
                    logger.debug("Received {} comments for {}", comments.length, result.id)

                    facebookPostData.likes = likes.map { from =>
                        new FacebookLike(
                            facebookPostId = facebookPostData.id,
                            facebookUserId = facebookPostData.facebookUser.id,
                            echoedUserId = facebookPostData.facebookUser.echoedUserId,
                            facebookId = from.id,
                            name = from.name)
                    }
                    facebookPostData.comments = comments.map { comment =>
                        new FacebookComment(
                                facebookPostId = facebookPostData.id,
                                facebookUserId = facebookPostData.facebookUser.id,
                                echoedUserId = facebookPostData.facebookUser.echoedUserId,
                                facebookId = comment.id,
                                byFacebookId = comment.from.id,
                                name = comment.from.name,
                                message = comment.message,
                                createdAt = comment.created_time)
                    }

                    self.channel ! GetPostDataResponse(msg, Right(facebookPostData))
                },
                {
                    logger.debug("No Facebook post data for {}", facebookPostData.facebookPost.facebookId)
                    self.channel ! GetPostDataResponse(
                            msg,
                            Left(FacebookException("Null response for %s" format facebookPostData.facebookPost.facebookId)))
                })
            } catch {
//                case e: BFE =>
                case e =>
                    logger.debug("Exception when retrieving data for FacebookPost {}: {}", facebookPostData.facebookPost.facebookId, e.getMessage)
                    self.channel ! GetPostDataResponse(msg, Left(FacebookException("%s: %s" format(e.getClass.getName, e.getMessage), e)))
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


class From {
    @BeanProperty var id: String = _
    @BeanProperty var name: String = _
}

class Application() {
    @BeanProperty var id: String = _
    @BeanProperty var name: String = _
    @BeanProperty var canvas_name: String = _
    @BeanProperty var namespace: String = _
}

class ListContainer[T] {
    @BeanProperty var count: Int = _
    @BeanProperty var data: List[T] = _
}

class Comment {
    @BeanProperty var id: String =_
    @BeanProperty var from: From = _
    @BeanProperty var message: String = _
    @BeanProperty var created_time: Date = _
}

class PostData() {
    @BeanProperty var id: String = _
    @BeanProperty var from: From = _
    @BeanProperty var message: String = _
    @BeanProperty var picture: String = _
    @BeanProperty var link: String = _
    @BeanProperty var name: String = _
    @BeanProperty var caption: String = _
    @BeanProperty var icon: String = _
    @BeanProperty var `type`: String = _
    @BeanProperty var application: Application = _
    @BeanProperty var created_time: String = _
    @BeanProperty var updated_time: String = _
    @BeanProperty var likes: ListContainer[From] = _
    @BeanProperty var comments: ListContainer[Comment] = _
}

case class Friend(
        id: String,
        name: String) {

    def createFacebookFriend(facebookUserId: String) = new FacebookFriend(
        facebookUserId,
        id,
        name)
}

class Me() {

    @BeanProperty var id: String = null
    @BeanProperty var name: String = null
    @BeanProperty var first_name: String = null
    @BeanProperty var middle_name: String = null
    @BeanProperty var last_name: String = null
    @BeanProperty var link: String = null
    @BeanProperty var gender: String = null
    @BeanProperty var email: String = null
    @BeanProperty var timezone: String = null
    @BeanProperty var locale: String = null
    @BeanProperty var verified: String = null
    @BeanProperty var updated_time: String = null

    def createFacebookUser(accessToken: String) = new FacebookUser(
        null,
        id,
        name,
        email,
        link,
        gender,
        timezone,
        locale,
        accessToken
    )

    def createFacebookTestUser(loginUrl: String, accessToken: String, password: String = "1234567890") = new FacebookTestUser(
        id,
        name,
        email,
        password,
        loginUrl,
        accessToken)
}




