package com.echoed.chamber.controllers.api

import org.springframework.stereotype.Controller
import com.echoed.chamber.controllers.CookieManager
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.reflect.BeanProperty
import com.echoed.chamber.services.echoeduser._
import org.eclipse.jetty.continuation.ContinuationSupport
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.services.feed._

@Controller
@RequestMapping(Array("/api"))
class UserController {

    private final val logger = LoggerFactory.getLogger(classOf[UserController])

    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var feedService: FeedService = _

    @BeanProperty var cookieManager: CookieManager = _
    @BeanProperty var closetView: String = _
    @BeanProperty var errorView: String = _

    @RequestMapping(value = Array("/me"), method = Array(RequestMethod.GET))
    @ResponseBody
    def me(
        @RequestParam(value="echoedUserId", required = false) echoedUserIdParam: String,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult("error")
        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).getOrElse(echoedUserIdParam)

        echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onSuccess {
            case LocateWithIdResponse(_, Right(echoedUserService)) =>
                echoedUserService.getProfile.onSuccess {
                    case GetProfileResponse(_, Right(profile)) =>
                        logger.debug("Found Echoed User {}", profile)
                        result.set(profile)
                }
        }

        result
    }

    
    @RequestMapping(value = Array("/me/feed"), method = Array(RequestMethod.GET))
    @ResponseBody
    def publicFeed(
            @RequestParam(value="page", required = false) page: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse): DeferredResult = {

        val result = new DeferredResult("error")

        val pageInt = try { Integer.parseInt(page) } catch { case _ => 0 }

        feedService.getPublicFeed(pageInt).onSuccess {
            case GetPublicFeedResponse(_,Right(feed)) =>
                logger.debug("Found feed of size {}", feed.echoes.size)
                result.set(feed)
            }
        result
    }
    
    @RequestMapping(value = Array("/feed/friends"), method = Array(RequestMethod.GET))
    @ResponseBody
    def feed(
            @RequestParam(value="echoedUserId", required = false) echoedUserIdParam:String,
            @RequestParam(value="page", required = false) page: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult("error")

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).getOrElse(echoedUserIdParam)

        val pageInt = try { Integer.parseInt(page) } catch { case _ => 0 }

        echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onSuccess {
            case LocateWithIdResponse(_, Right(echoedUserService)) =>
                echoedUserService.getFeed(pageInt).onSuccess {
                    case GetFeedResponse(_, Right(feed)) =>
                        result.set(feed)
                }
        }

        result
    }

    @RequestMapping(value = Array("/me/exhibit"), method = Array(RequestMethod.GET))
    @ResponseBody
    def exhibit(
            @RequestParam(value = "echoedUserId", required = false) echoedUserIdParam: String,
            @RequestParam(value = "page", required = false) page: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult("error")

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).getOrElse(echoedUserIdParam)

        val pageInt = try { Integer.parseInt(page) } catch { case _ => 0 }

        echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onSuccess {
            case LocateWithIdResponse(_, Right(echoedUserService)) =>
                echoedUserService.getCloset(pageInt).onSuccess {
                    case GetExhibitResponse(_, Right(closet)) =>
                        logger.debug("Received for {} exhibit of {} echoes", echoedUserId, closet.echoes.size)
                        result.set(closet)
                }
        }

        result
    }

    @RequestMapping(value = Array("/me/friends"), method = Array(RequestMethod.GET))
    @ResponseBody
    def friends(
            @RequestParam(value = "echoedUserId", required = false) echoedUserIdParam: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult("error")

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).getOrElse(echoedUserIdParam)

        echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onSuccess {
            case LocateWithIdResponse(_, Right(echoedUserService)) =>
                echoedUserService.getFriends.onSuccess{
                    case GetEchoedFriendsResponse(_, Right(friends)) => result.set(friends)
                }
        }

        result
    }

    @RequestMapping(value = Array("/category/{categoryId}"), method=Array(RequestMethod.GET))
    @ResponseBody
    def categoryFeed(
                       @PathVariable(value="categoryId") categoryId: String,
                       @RequestParam(value="page", required = false) page: String,
                       httpServletRequest: HttpServletRequest,
                       httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult("error")

        logger.debug("Requesting for Category Feed for Category {}", categoryId )

        val pageInt = try { Integer.parseInt(page) } catch { case _ => 0 }

        feedService.getPublicCategoryFeed(categoryId, pageInt).onSuccess {
            case GetPublicCategoryFeedResponse(_, Right(feed)) => result.set(feed)
        }
        result
    }


    @RequestMapping(value = Array("/partner/{partnerId}"), method=Array(RequestMethod.GET))
    @ResponseBody
    def partnerFeed(
            @PathVariable(value="partnerId") partnerId: String,
            @RequestParam(value="page", required = false) page: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult("error")

        logger.debug("Requesting for Partner Feed for Partner {}", partnerId )

        val pageInt = try { Integer.parseInt(page) } catch { case _ => 0 }

        feedService.getPartnerFeed(partnerId, pageInt).onSuccess {
            case GetPartnerFeedResponse(_, Right(partnerFeed)) => result.set(partnerFeed)
        }
        result
    }

    @RequestMapping(value= Array("/user/{id}"), method=Array(RequestMethod.GET))
    @ResponseBody
    def friendExhibit(
            @PathVariable(value="id") echoedFriendId: String,
            @RequestParam(value= "page", required = false) page: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        logger.debug("echoedFriendId: {}", echoedFriendId)

        val result = new DeferredResult("error")

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest)

        var pageInt = try { Integer.parseInt(page) } catch { case _ => 0 }

        echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId.get).onSuccess {
            case LocateWithIdResponse(_,Right(echoedUserService)) =>
                echoedUserService.getFriendCloset(echoedFriendId, pageInt).onSuccess {
                    case GetFriendExhibitResponse(_, Right(closet)) => result.set(closet)
                }
        }

        result
    }
}
