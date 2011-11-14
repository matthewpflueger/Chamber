package com.echoed.chamber.controllers


import com.echoed.chamber.services.twitter.TwitterServiceLocator
import com.echoed.chamber.services.twitter.TwitterService
import org.springframework.web.bind.annotation.{CookieValue, RequestParam, RequestMapping, RequestMethod}
import org.springframework.stereotype.Controller
import reflect.BeanProperty
import akka.dispatch.Future
import collection.mutable.WeakHashMap
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import java.util.Date
import org.slf4j.LoggerFactory
import twitter4j.auth.{RequestToken,AccessToken}
import javax.servlet.http.HttpSession
import twitter4j.{TwitterFactory, Twitter, TwitterException, User}
import org.springframework.beans.factory.annotation.Autowired
import com.echoed.chamber.dao.TwitterUserDao
import com.echoed.chamber.domain.TwitterUser
import com.echoed.chamber.services.EchoService
import com.echoed.chamber.services.echoeduser.EchoedUserServiceLocator
import com.echoed.chamber.services.echoeduser.EchoedUserService
import org.eclipse.jetty.continuation.ContinuationSupport

@Controller
@RequestMapping(Array("/twitter"))
class TwitterController {

  private val logger = LoggerFactory.getLogger(classOf[TwitterController])


  @BeanProperty var twitterUserDao: TwitterUserDao = null
  @BeanProperty var twitterRedirectUrl: String = null
  @Autowired @BeanProperty var twitterServiceLocator: TwitterServiceLocator = _
  @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
  @BeanProperty var echoService: EchoService = _


  //@BeanProperty var twitterLoginErrorView: String = _
  @BeanProperty var confirmView: String = _
  @BeanProperty var dashboardView: String = _

  @RequestMapping(method = Array(RequestMethod.GET))
  def twitter(@RequestParam("oauth_token") oAuthToken: String,
              @RequestParam("oauth_verifier") oAuthVerifier: String,
              httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Unit = {

    val continuation = ContinuationSupport.getContinuation(httpServletRequest)
    if( (Option(oAuthToken) == None && Option(oAuthVerifier)==None) ||
        continuation.isExpired
        ){
      var t:TwitterService = twitterServiceLocator.getTwitterService().get.asInstanceOf[TwitterService]
      httpServletResponse.sendRedirect(t.getRequestToken().get.asInstanceOf[RequestToken].getAuthenticationURL)
    }
  }

  @RequestMapping(value = Array("/login"), method = Array(RequestMethod.GET))
  def login(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Unit ={
    val oAuthToken: String = httpServletRequest.getParameter("oauth_token")
    val oAuthVerifier: String = httpServletRequest.getParameter("oauth_verifier")


    var t:TwitterService = twitterServiceLocator.getTwitterServiceWithToken(oAuthToken).get.asInstanceOf[TwitterService]
    var accessToken: AccessToken = t.getAccessToken(oAuthVerifier).get.asInstanceOf[AccessToken]



    var t2 :TwitterService = twitterServiceLocator.getTwitterServiceWithAccessToken(accessToken.getToken,accessToken.getTokenSecret).get.asInstanceOf[TwitterService]
    val me: TwitterUser = t2.getMe(accessToken.getToken,accessToken.getTokenSecret).get.asInstanceOf[TwitterUser]
    httpServletResponse.getWriter.println(me.username)

  }
}