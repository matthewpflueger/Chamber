package com.echoed.chamber.services.facebook

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
import com.echoed.util.ScalaObjectMapper
import java.util.{Date, Properties}
import java.net.{HttpURLConnection, URL}
import com.google.common.io.ByteStreams
import akka.actor.{Channel, Actor}
import java.util.concurrent.ConcurrentHashMap
import collection.mutable.ConcurrentMap
import java.io.{OutputStreamWriter, InputStream}


class FacebookAccessActor extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[FacebookAccessActor])

    @BeanProperty var clientId: String = _
    @BeanProperty var clientSecret: String = _
    @BeanProperty var redirectUrl: String = _
    @BeanProperty var canvasApp: String = _
    @BeanProperty var properties: Properties = _
    @BeanProperty var appNameSpace: String = _

    private val cache: ConcurrentMap[String, FacebookBatcher] = new ConcurrentHashMap[String, FacebookBatcher]()

    override def preStart {
        //NOTE: getting the properties like this is necessary due to a bug in Akka's Spring integration
        //where placeholder values were not being resolved
        {
            if (clientId == null) clientId = properties.getProperty("clientId")
            if (clientSecret == null) clientSecret = properties.getProperty("clientSecret")
            if (redirectUrl == null) redirectUrl = properties.getProperty("redirectUrl")
            if (canvasApp == null) canvasApp = properties.getProperty("canvasApp")
            if (appNameSpace == null) appNameSpace = properties.getProperty("appNameSpace")
            clientId != null && clientSecret != null && redirectUrl != null && canvasApp != null
        } ensuring (_ == true, "Missing parameters")
    }


    def fetchMe(accessToken: String) = {
        getFacebookBatcher(accessToken)
                .graph("me", new TypeReference[Me] {})
                .get
                .createFacebookUser(accessToken)
    }

    def receive = {
        case msg @ FetchMe(accessToken) =>
            val channel: Channel[FetchMeResponse] = self.channel

            try {
                val facebookUser = fetchMe(accessToken)
                channel ! FetchMeResponse(msg, Right(facebookUser))
            } catch {
                case e =>
                    channel ! FetchMeResponse(msg, Left(FacebookException("Could not get Facebook user", e)))
                    logger.error("Error processing %s" format msg, e)
            }


        case msg @ GetMe(code, queryString) =>
            val channel: Channel[GetMeResponse] = self.channel

            try {
                val redirect = redirectUrl + queryString
                logger.debug("Requesting access token for code {}, redirect url {}", code, redirect)
                val accessToken = FacebookBatcher.getAccessToken(clientId, clientSecret, code, redirect)
                logger.debug("Requesting me with access token {}", accessToken)
                val facebookUser = fetchMe(accessToken)
                channel ! GetMeResponse(msg, Right(facebookUser))
                logger.debug("Got me {}", facebookUser)
            } catch {
                case e =>
                    channel ! GetMeResponse(msg, Left(FacebookException("Could not get Facebook user", e)))
                    logger.error("Error processing %s" format msg, e)
            }


        case msg @ GetFriends(accessToken, facebookId, facebookUserId) =>
            val channel: Channel[GetFriendsResponse] = self.channel

            try {
                logger.debug("Requesting friends for {} with access token {}", facebookId, accessToken)
                val pagedFriends = getFacebookBatcher(accessToken).graph(
                        ("%s/friends" format facebookId),
                        new TypeReference[Paged[Friend]] {}).get.getData
                logger.debug("Found {} friends for FacebookUser {}", pagedFriends.size(), facebookUserId)
                val facebookFriends = asScalaBuffer(pagedFriends).map(_.createFacebookFriend(facebookUserId)).toList
                channel ! GetFriendsResponse(msg, Right(facebookFriends))
                logger.debug("Sent {} friends for FacebookUser {}", facebookFriends.length, facebookUserId)
            } catch {
                case e: BFE =>
                    channel ! GetFriendsResponse(
                        msg,
                        Left(FacebookException("Error fetching Facebook friends", e)))
                    logger.error("Error processing %s" format msg, e)
            }


        case msg @ Post(accessToken, facebookId, facebookPost) =>
            val channel: Channel[PostResponse] = self.channel

            try {
                logger.debug("Creating new post for {} with access token {}", facebookId, accessToken)
                val result = getFacebookBatcher(accessToken).post(
                        ("%s/feed" format facebookId),
                        new Param("name", facebookPost.name),
                        new Param("message", facebookPost.message),
                        new Param("picture", facebookPost.picture),
                        new Param("link", facebookPost.link),
                        new Param("caption", facebookPost.caption),
                        new Param("actions", Array[Action] {
                            new Action("View Echoed Exhibit", canvasApp)
                        })).get()
                val fp = facebookPost.copy(facebookId = result)
                channel ! PostResponse(msg, Right(fp))
                logger.debug("Successfully posted {}", fp)
            } catch {
                case e =>
                    channel ! PostResponse(msg, Left(FacebookException("Could not post to Facebook", e)))
                    logger.error("Error processing %s" format msg, e)
            }

        case msg @ PublishAction(accessToken, action, obj, objUrl) =>
            val channel: Channel[PublishActionResponse] = self.channel

            val url = new URL("https://graph.facebook.com/me/%s:%s?" format(appNameSpace, action))
            val urlParameters = "%s=%s&access_token=%s" format(obj, objUrl, accessToken)
            logger.debug("urlParamters: {}", urlParameters)
            var connection: Option[HttpURLConnection] = None
            var inputStream: Option[InputStream] = None

            logger.debug("Publishing Action: %s?%s=%s" format (action, obj, objUrl))
            connection = Option(url.openConnection().asInstanceOf[HttpURLConnection])
            try {
                connection.get.setRequestMethod("POST")
                connection.get.setDoOutput(true)
                val wr: OutputStreamWriter = new OutputStreamWriter(connection.get.getOutputStream)
                wr.write(urlParameters)
                wr.flush()

                if(connection.get.getResponseCode >= 400){
                    inputStream = Option(connection.get.getErrorStream)
                    val bytes = ByteStreams.toByteArray(inputStream.get)
                    logger.debug("Server returned HTTP response Code 400:  Publishing Action Response %s" format new String(bytes, "UTF-8") )
                } else {
                    inputStream = Option(connection.get.getInputStream)
                    val bytes = ByteStreams.toByteArray(inputStream.get)
                    logger.debug("Received Publish Action Response %s" format new String(bytes, "UTF-8"))
                }
            } catch {
                case e =>
                    logger.debug("Error processing %s" format e)
            }


        case msg @ GetPostData(facebookPostData) =>
            val channel: Channel[GetPostDataResponse] = self.channel

            def deserializeFacebookPost(bytes: Array[Byte]) {
                val resultOption = Option(new ScalaObjectMapper().readValue(bytes, classOf[PostData]))

                //TODO this does not work and I have no idea why - always throws an exception
//                val resultOption = Option(getFacebookBatcher(accessToken)
//                        .graph("%s" format facebookId, classOf[PostData])
//                        .get)

                resultOption.cata({ result =>
                    val likes:List[From] = Option(result.likes).cata(
                        cs => Option(cs.data).cata(
                            data => data,
                            List[From]()),
                        List[From]())

                    val comments:List[Comment] = Option(result.comments).cata(
                        cs => Option(cs.data).cata(
                            data => data,
                            List[Comment]()),
                        List[Comment]())

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

                    channel ! GetPostDataResponse(msg, Right(facebookPostData))
                },
                {
                    logger.debug("No Facebook post data for {}", facebookPostData.facebookPost.facebookId)
                    channel ! GetPostDataResponse(
                            msg,
                            Left(FacebookException("Null response for %s" format facebookPostData.facebookPost.facebookId)))
                })
            }


            var connection: Option[HttpURLConnection] = None
            var inputStream: Option[InputStream] = None
            try {
                val facebookId = facebookPostData.facebookPost.facebookId
                val accessToken = facebookPostData.facebookUser.accessToken

                logger.debug("Retrieving data for FacebookPost {}", facebookId)
                val url = new URL("https://graph.facebook.com/%s?access_token=%s" format(facebookId, accessToken))
                connection = Option(url.openConnection().asInstanceOf[HttpURLConnection])
                connection.get.connect
                connection.get.getResponseCode match {
                    case good if (good >= 200 && good < 300) =>
                        inputStream = Option(connection.get.getInputStream)
                        val bytes = ByteStreams.toByteArray(inputStream.get)
                        if (new String(bytes, "UTF-8") == "false") {
                            logger.debug("Fetch of Facebook post {} returned false", facebookPostData.facebookPost.facebookId)
                            channel ! GetPostDataResponse(msg, Left(GetPostDataFalse(facebookPost = facebookPostData.facebookPost)))
                        } else {
                            deserializeFacebookPost(bytes)
                        }

                    case bad if (bad >= 400 && bad < 600) =>
                        inputStream = Option(connection.get.getErrorStream)
                        val bytes = ByteStreams.toByteArray(inputStream.get)
                        if (logger.isDebugEnabled) {
                            logger.debug("Error response for post {} is: {}", facebookPostData.facebookPost.id, new String(bytes, "UTF-8"))
                        }
                        val errorContainer = (new ScalaObjectMapper().readValue(bytes, classOf[ErrorContainer]))
                        if (errorContainer == null || errorContainer.error == null || errorContainer.error.`type` == null) {
                            //no additional info?!?!
                            throw new FacebookException("Unexpected response code %s" format bad)
                        }
                        if (errorContainer.error.`type` == "OAuthException") {
                            channel ! GetPostDataResponse(msg, Left(GetPostDataOAuthError(
                                facebookPostData.facebookPost,
                                errorContainer.error.`type`,
                                errorContainer.error.code,
                                errorContainer.error.message)))
                        } else {
                            channel ! GetPostDataResponse(msg, Left(GetPostDataError(
                                    facebookPostData.facebookPost,
                                    errorContainer.error.`type`,
                                    errorContainer.error.code,
                                    errorContainer.error.message)))
                        }

                    case other =>
                        throw new FacebookException("Unexpected response code %s" format other)
                }

            } catch {
                case fe: FacebookException =>
                    channel ! GetPostDataResponse(msg, Left(fe))
                case e =>
                    logger.error("Exception when retrieving data for FacebookPost {}: {}", facebookPostData.facebookPost.facebookId, e)
                    channel ! GetPostDataResponse(msg, Left(FacebookException("%s: %s" format(e.getClass.getName, e.getMessage), e)))
            } finally {
                inputStream.foreach(_.close)
                connection.foreach(_.disconnect())
            }


        case msg @ Logout(accessToken) =>
            try {
                removeFacebookBatcher(accessToken).cata(
                    facebookBatcher => {
                        logger.debug("Removed FacebookBatcher from cache for accessToken {}", accessToken)
                        self.channel ! LogoutResponse(msg, Right(true))

                        //this does not work - seems there is no way to actually logout of Facebook via the backend
//                        val url = new URL("https://www.facebook.com/logout.php?next=%s&access_token=%s" format(redirectUrl, accessToken))
//                        val connection = url.openConnection().asInstanceOf[HttpURLConnection]
//                        connection.setConnectTimeout(5000)
//                        connection.setReadTimeout(5000)
//
//                        val logoutResponse = "%s:%s" format(connection.getResponseCode, connection.getResponseMessage)
//                        logger.debug("Logout of {} returned {}", accessToken, logoutResponse)
                    },
                    {
                        self.channel ! LogoutResponse(msg, Right(false))
                        logger.debug("Did not find FacebookBatcher for {}", accessToken)
                    }
                )
            } catch {
                case e =>
                    self.channel ! LogoutResponse(msg, Left(FacebookException("Could not logout from Facebook")))
                    logger.error("Unexpected error processing %s" format msg, e)
            }
    }


    private def removeFacebookBatcher(accessToken: String) = cache.remove(accessToken)


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


//{
//    "error": {
//        "message": "Error validating access token: Session has expired at unix time 1333861200. The current unix time is 1333916314.",
//        "type": "OAuthException",
//        "code": 190
//    }
//}
class ErrorContainer {
    @BeanProperty var error: Error = _
}

class Error {
    @BeanProperty var message: String = _
    @BeanProperty var `type`: String = _
    @BeanProperty var code: Int = _
}

class From {
    @BeanProperty var id: String = _
    @BeanProperty var name: String = _
}

class Application {
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

class Action(
    @BeanProperty var name: String,
    @BeanProperty var link: String) {

    def this() = this(null, null)
}

class Privacy {
    @BeanProperty var description: String = _
    @BeanProperty var value: String = _
    @BeanProperty var allow: String = _
    @BeanProperty var deny: String = _
}

//Example...
//{"id":"100003326847181_152036588250568",
// "from":{"name":"Evan Echoed","id":"100003326847181"},
// "message":"Love these t-shirts!!!",
// "picture":"http:\/\/platform.ak.fbcdn.net\/www\/app_full_proxy.php?app=323745504330890&v=1&size=z&cksum=d7984f095606cfce7721249e577f7234&src=http\u00253A\u00252F\u00252Fcdn.shopify.com\u00252Fs\u00252Ffiles\u00252F1\u00252F0094\u00252F3522\u00252Fproducts\u00252Ftees2_medium.jpg\u00253F1963",
// "link":"http:\/\/demo.echoed.com\/echo\/86dd6bcf-1342-46a9-84e9-b1831c7c4965\/45be2f22-3e0d-4e48-b009-253680e91a38",
// "name":"The Hangover Baby Carrier by Tees For All",
// "caption":"This is My Description",
// "icon":"http:\/\/photos-d.ak.fbcdn.net\/photos-ak-snc1\/v85005\/110\/323745504330890\/app_2_323745504330890_8758.gif",
// "actions":[
//      {"name":"Comment","link":"http:\/\/www.facebook.com\/100003326847181\/posts\/152036588250568"},
//      {"name":"Like","link":"http:\/\/www.facebook.com\/100003326847181\/posts\/152036588250568"},
//      {"name":"View Echoed Exhibit","link":"http:\/\/apps.facebook.com\/echoeddemo\/"}],
// "privacy":{"description":"Friends","value":"ALL_FRIENDS","allow":"0","deny":"0"},
// "type":"link",
// "application":{"name":"EchoedDemo","canvas_name":"echoeddemo","namespace":"echoeddemo","id":"323745504330890"},
// "created_time":"2012-02-24T18:10:23+0000",
// "updated_time":"2012-02-24T18:10:23+0000",
// "comments":{"count":0},
// "is_published":true}
class PostData {
    @BeanProperty var id: String = _
    @BeanProperty var from: From = _
    @BeanProperty var message: String = _
    @BeanProperty var picture: String = _
    @BeanProperty var link: String = _
    @BeanProperty var name: String = _
    @BeanProperty var caption: String = _
    @BeanProperty var icon: String = _
    @BeanProperty var description: String = _
    @BeanProperty var actions: List[Action] = _
    @BeanProperty var privacy: Privacy = _
    @BeanProperty var `type`: String = _
    @BeanProperty var application: Application = _
    @BeanProperty var created_time: String = _
    @BeanProperty var updated_time: String = _
    @BeanProperty var likes: ListContainer[From] = _
    @BeanProperty var comments: ListContainer[Comment] = _
    @BeanProperty var is_published: Boolean = _
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




