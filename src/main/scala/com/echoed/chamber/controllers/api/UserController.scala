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

@Controller
@RequestMapping(Array("/user"))
class UserController {

    private final val logger = LoggerFactory.getLogger(classOf[UserController])

    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _

    @BeanProperty var cookieManager: CookieManager = _
    @BeanProperty var closetView: String = _
    @BeanProperty var errorView: String = _

    @RequestMapping(value = Array("/me"), method = Array(RequestMethod.GET))
    @ResponseBody
    def me(
        @RequestParam(value="echoedUserId", required = false) echoedUserIdParam:String,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse) = {

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).getOrElse(echoedUserIdParam)

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired){
            new ModelAndView(errorView)
        } else Option(continuation.getAttribute("jsonResponse")).getOrElse({
            continuation.suspend(httpServletResponse)

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onResult {
                case LocateWithIdResponse(_, Left(error)) =>
                    logger.error("Error locating EchoedUserService for user %s " format echoedUserId, error)
                case LocateWithIdResponse(_, Right(echoedUserService)) =>
                    echoedUserService.getProfile.onResult {
                        case GetProfileResponse(_, Left(error)) => throw new RuntimeException("Unknown Response %s" format error)
                        case GetProfileResponse(_, Right(profile)) =>
                            logger.debug("Found Echoed User {}", profile)
                            continuation.setAttribute("jsonResponse", profile)
                            continuation.resume()
                        case unknown => throw new RuntimeException("Unknown Response %s" format unknown)
                    }
            }

            continuation.undispatch()
        })
    }

    
    @RequestMapping(value = Array("/feed/public"), method = Array(RequestMethod.GET))
    @ResponseBody
    def publicFeed(
            @RequestParam(value="echoedUserId", required = false) echoedUserIdParam:String,
            @RequestParam(value="page", required = false) page: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).getOrElse(echoedUserIdParam)

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired){
            logger.error("Request expired to view exhibit for user{}", echoedUserId)
            new ModelAndView(errorView)
        } else Option(continuation.getAttribute("feed")).getOrElse({
            continuation.suspend(httpServletResponse)

            var pageInt: Int = 0;
            try {
                pageInt = Integer.parseInt(page);
            } catch {
                case nfe:NumberFormatException =>
                    pageInt = 0;
            }
            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onResult {
                case LocateWithIdResponse(_,Left(error))=>
                    logger.error("Error locating EchoedUserService for user %s" format echoedUserId, error)
                case LocateWithIdResponse(_,Right(echoedUserService)) =>
                    echoedUserService.getPublicFeed(pageInt).onResult{
                        case GetPublicFeedResponse(_,Left(error)) => throw new RuntimeException("Unknown Response %s" format error)
                        case GetPublicFeedResponse(_,Right(feed)) =>
                            logger.debug("Found feed of size {}", feed.echoes.size)
                            continuation.setAttribute("feed", feed)
                            continuation.resume()
                        case unknown => throw new RuntimeException("Unknown Response %s" format unknown)
                    }
            }
                .onException{
                case e =>
                    logger.error("Exception thrown Locating EchoedUserService for user %s" format echoedUserId, e)
            }
            continuation.undispatch()
        })

    }
    
    @RequestMapping(value = Array("/feed/friends"), method = Array(RequestMethod.GET))
    @ResponseBody
    def feed(
            @RequestParam(value="echoedUserId", required = false) echoedUserIdParam:String,
            @RequestParam(value="page", required = false) page: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).getOrElse(echoedUserIdParam)

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired){
            logger.error("Request expired to view exhibit for user %s" format echoedUserId)
            new ModelAndView(errorView)
        } else Option(continuation.getAttribute("feed")).getOrElse({
            continuation.suspend(httpServletResponse)

            var pageInt: Int = 0;
            try {
                pageInt = Integer.parseInt(page);
            } catch {
                case nfe:NumberFormatException =>
                    pageInt = 0;
            }

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onResult {
                case LocateWithIdResponse(_,Left(error))=>
                    logger.error("Error locating EchoedUserService: {}", error)
                case LocateWithIdResponse(_,Right(echoedUserService)) =>
                    echoedUserService.getFeed(pageInt).onResult{
                        case GetFeedResponse(_,Left(error)) => throw new RuntimeException("Unknown Response %s" format error)
                        case GetFeedResponse(_,Right(feed)) =>
                            continuation.setAttribute("feed", feed)
                            continuation.resume()
                        case unknown => throw new RuntimeException("Unknown Response %s" format unknown)
                    }
            }
                .onException{
                case e =>
                    logger.error("Exception thrown Locating EchoedUserService for %s" format echoedUserId, e)
            }
            continuation.undispatch()
        })

    }

    @RequestMapping(value = Array("/exhibit"), method = Array(RequestMethod.GET))
    @ResponseBody
    def exhibit(
            @RequestParam(value = "echoedUserId", required = false) echoedUserIdParam: String,
            @RequestParam(value = "page", required = false) page: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).getOrElse(echoedUserIdParam)

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired) {
            logger.error("Request expired to view exhibit for user %s" format echoedUserId)
            new ModelAndView(errorView)
        } else Option(continuation.getAttribute("exhibit")).getOrElse({
            continuation.suspend(httpServletResponse)

            var pageInt: Int = 0;
            try {
                pageInt = Integer.parseInt(page);
            } catch {
                case nfe:NumberFormatException =>
                    pageInt = 0;
            }

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onResult {
                case LocateWithIdResponse(_,Left(error)) =>
                    logger.error("Error Locating EchoedUserService With Id %s" format echoedUserId, error)
                case LocateWithIdResponse(_,Right(echoedUserService)) =>
                    echoedUserService.getCloset(pageInt).onResult {
                        case GetExhibitResponse(_,Left(error)) =>
                            logger.error("Error Getting Closet for %s" format echoedUserId, error)
                            throw new RuntimeException("Unknown Response %s" format error)
                        case GetExhibitResponse(_,Right(closet)) =>
                            logger.debug("Received for {} exhibit of {} echoes", echoedUserId, closet.echoes.size)
                            continuation.setAttribute("exhibit", closet)
                            continuation.resume()
                        case unknown => throw new RuntimeException("Unknown Response %s" format unknown)
                    }
                        .onException{
                        case e =>
                            logger.error("Exception thrown Getting Exhibit %s" format echoedUserId, e)
                    }
            }
                .onException{
                case e=>
                    logger.error("Exception thrown when Locating EchoedUserService %s" format echoedUserId, e)
            }
            continuation.undispatch()
        })
    }

    @RequestMapping(value = Array("/friends"), method = Array(RequestMethod.GET))
    @ResponseBody
    def friends(
            @RequestParam(value = "echoedUserId", required = false) echoedUserIdParam: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).getOrElse(echoedUserIdParam)

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired) {
            logger.error("Request expired to view friends for user %s" format echoedUserId)
            new ModelAndView(errorView)
        } else Option(continuation.getAttribute("friends")).getOrElse({
            continuation.suspend(httpServletResponse)

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onResult {
                case LocateWithIdResponse(_, Left(error)) =>
                    logger.error("Error Locating EchoedUserService for user %s" format echoedUserId, error)
                case LocateWithIdResponse(_, Right(echoedUserService)) =>
                    echoedUserService.getFriends.onResult{
                        case GetEchoedFriendsResponse(_,Left(error)) =>
                            logger.error("Error Getting Friends for user %s" format echoedUserId, error)
                            continuation.setAttribute("friends", error);
                            continuation.resume()
                        case GetEchoedFriendsResponse(_,Right(friends)) =>
                            continuation.setAttribute("friends", friends)
                            continuation.resume()
                        case unknown =>
                            continuation.setAttribute("friends", unknown)
                            continuation.resume()
                            throw new RuntimeException("Unknown Response %s" format unknown)
                    }
                        .onException{
                        case e=>
                            logger.error("Exception Thrown Getting Friends for user %s" format echoedUserId, e)
                            continuation.setAttribute("friends", e)
                            continuation.resume()
                    }
            }
                .onException{
                case e =>
                    logger.error("Exception thrown Locating EchoedUserService for user %s" format echoedUserId, e)
                    continuation.setAttribute("friends",e)
                    continuation.resume()
            }
            continuation.undispatch()
        })
    }
    
    @RequestMapping(value = Array("/feed/partner/{name}"), method=Array(RequestMethod.GET))
    @ResponseBody
    def partnerFeed(
            @RequestParam(value = "echoedUserId", required = false) echoedUserIdParam: String,
            @PathVariable(value="name") partnerName: String,
            @RequestParam(value="page", required=false) page: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).getOrElse(echoedUserIdParam)
        
        logger.debug("Requesting for Partner Feed for Partner {}", partnerName )
        val continuation = ContinuationSupport.getContinuation(httpServletRequest)

        if(continuation.isExpired){
        } else Option(continuation.getAttribute("jsonResponse")).getOrElse({
            continuation.suspend(httpServletResponse)

            var pageInt: Int = 0;
            try {
                pageInt = Integer.parseInt(page);
            } catch {
                case nfe:NumberFormatException =>
                    pageInt = 0;
            }

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onResult{
                case LocateWithIdResponse(_, Left(error)) =>
                    logger.error("Error Locating EchoedUserService for user %s" format echoedUserId, error)
                case LocateWithIdResponse(_, Right(echoedUserService)) =>
                    echoedUserService.getPartnerFeed(partnerName, pageInt).onResult{
                        case GetPartnerFeedResponse(_, Left(error)) =>
                            logger.error("Error Getting Partner Feed for partne %s" format partnerName , error)
                        case GetPartnerFeedResponse(_, Right(partnerFeed)) =>
                            continuation.setAttribute("jsonResponse", partnerFeed)
                            continuation.resume()
                    }
                    .onException{
                        case e =>
                            logger.error("Exception thrown getting partner feed for partner %s" format partnerName, e)
                            continuation.setAttribute("jsonResponse", e)
                            continuation.resume()
                    }
            }
            .onException{
                case e =>
                    logger.error("Exception thrown when Locating EchoedUserService for user %s" format echoedUserId, e)
                    continuation.setAttribute("jsonResponse", e)
                    continuation.resume()
            }
            
            continuation.undispatch()
        })
    }

    @RequestMapping(value= Array("/exhibit/{id}"), method=Array(RequestMethod.GET))
    @ResponseBody
    def friendExhibit(
            @PathVariable(value="id") echoedFriendId: String,
            @RequestParam(value= "page", required= false) page: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {



        logger.debug("echoedFriendId: {}", echoedFriendId)
        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if(continuation.isExpired) {

        } else Option(continuation.getAttribute("closet")).getOrElse({
            continuation.suspend(httpServletResponse)

            var pageInt: Int = 0;
            try {
                pageInt = Integer.parseInt(page);
            } catch {
                case nfe:NumberFormatException =>
                    pageInt = 0;
            }


            val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest)

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId.get).onResult {
                case LocateWithIdResponse(_,Left(error)) =>
                    logger.error("Error Locating EchoedUserService for user %s" format echoedUserId, error)
                case LocateWithIdResponse(_,Right(echoedUserService)) =>
                    echoedUserService.getFriendCloset(echoedFriendId, pageInt).onResult{
                        case GetFriendExhibitResponse(_,Left(error)) =>
                            logger.error("Error Getting Friend's Closet for user %s" format echoedUserId, error)
                        case GetFriendExhibitResponse(_,Right(closet)) =>
                            continuation.setAttribute("closet",closet)
                            continuation.resume()
                    }
                        .onException{
                        case e=>
                            logger.error("Exception thrown Getting Friend's Closet for user %s" format echoedUserId, e)
                            continuation.setAttribute("closet", e)
                            continuation.resume()
                    }
            }
                .onException{
                case e =>
                    logger.error("Exception thrown when Locating EchoedUserService for user %s" format echoedUserId, e)
                    continuation.setAttribute("closet", e)
                    continuation.resume();
            }

            continuation.undispatch()
        })
    }
}
