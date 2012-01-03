package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
//import com.echoed.chamber.domain.EchoPossibility
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.reflect.BeanProperty
//import com.echoed.chamber.services.echoeduser.EchoedUserServiceLocator
import com.echoed.chamber.services.echoeduser._
import org.eclipse.jetty.continuation.ContinuationSupport
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.ModelAndView



@Controller
@RequestMapping(Array("/closet"))
class ClosetController {

    private final val logger = LoggerFactory.getLogger(classOf[ClosetController])

    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _

    @BeanProperty var closetView: String = _
    @BeanProperty var errorView: String = _

    @RequestMapping(method = Array(RequestMethod.GET))
    def closet(
            @CookieValue(value = "echoedUserId", required = true) echoedUserId: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired) {
            logger.error("Request expired to view closet for user {}", echoedUserId)
            new ModelAndView(errorView)
        } else Option(continuation.getAttribute("modelAndView")).getOrElse({
            continuation.suspend(httpServletResponse)

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onResult {
                case LocateWithIdResponse(_,Left(error)) => throw new RuntimeException("Unknown Response %s" format error)
                case LocateWithIdResponse(_,Right(echoedUserService)) =>
                    logger.debug("Found EchoedUserService {} ", echoedUserService);

                    /*echoedUserService.getEchoedUser().map {
                        echoedUser =>
                            val modelAndView = new ModelAndView(closetView)
                            modelAndView.addObject("echoedUser",echoedUser)
                            modelAndView.addObject("totalCredit","0");
                            continuation.setAttribute("modelAndView",modelAndView)
                            continuation.resume()
                    }  */
                    echoedUserService.getCloset.onResult{
                        case GetExhibitResponse(_,Left(error)) => throw new RuntimeException("Unknown Response %s" format error)
                        case GetExhibitResponse(_,Right(closet)) =>
                            val modelAndView = new ModelAndView(closetView)
                            modelAndView.addObject("echoedUser", closet.echoedUser)
                            modelAndView.addObject("echoes", closet.echoes)
                            modelAndView.addObject("totalCredit", closet.totalCredit.round)
                            continuation.setAttribute("modelAndView", modelAndView)
                            continuation.resume()
                        case unknown => throw new RuntimeException("Unknown Response %s" format unknown)
                }
            }

            continuation.undispatch()
        })
    }

    @RequestMapping(value = Array("/feed"), method = Array(RequestMethod.GET))
    @ResponseBody
    def feed(
            @CookieValue(value = "echoedUserId", required= false) echoedUserIdCookie:String,
            @RequestParam(value="echoedUserId", required = false) echoedUserIdParam:String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        var echoedUserId: String = null;
        if(echoedUserIdCookie != null){
            echoedUserId = echoedUserIdCookie;
        }
        else{
            echoedUserId = echoedUserIdParam;
        }

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired){
            logger.error("Request expired to view exhibit for user{}", echoedUserId)
            new ModelAndView(errorView)
        } else Option(continuation.getAttribute("feed")).getOrElse({
            continuation.suspend(httpServletResponse)

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onResult {
                case LocateWithIdResponse(_,Left(error))=>
                case LocateWithIdResponse(_,Right(echoedUserService)) =>
                    echoedUserService.getFeed.onResult{
                        case GetFeedResponse(_,Left(error)) => throw new RuntimeException("Unknown Response %s" format error)
                        case GetFeedResponse(_,Right(feed)) =>
                            continuation.setAttribute("feed", feed.echoes)
                            continuation.resume()
                        case unknown => throw new RuntimeException("Unknown Response %s" format unknown)
                }
            }
            continuation.undispatch()
        })

    }
    
    @RequestMapping(value = Array("/exhibit"), method = Array(RequestMethod.GET))
    @ResponseBody
    def exhibit(
            @CookieValue(value = "echoedUserId", required = false) echoedUserIdCookie: String,
            @RequestParam(value = "echoedUserId", required = false) echoedUserIdParam: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {


        var echoedUserId:String = null;
        if(echoedUserIdCookie != null){
            echoedUserId = echoedUserIdCookie;
        }
        else{
            echoedUserId = echoedUserIdParam;
        }

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired) {
            logger.error("Request expired to view exhibit for user {}", echoedUserId)
            new ModelAndView(errorView)
        } else Option(continuation.getAttribute("exhibit")).getOrElse({
            continuation.suspend(httpServletResponse)

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onResult {
                case LocateWithIdResponse(_,Left(error)) =>
                case LocateWithIdResponse(_,Right(echoedUserService)) =>
                    echoedUserService.getCloset.onResult {
                        case GetExhibitResponse(_,Left(error)) => throw new RuntimeException("Unknown Response %s" format error)
                        case GetExhibitResponse(_,Right(closet)) =>
                            continuation.setAttribute("exhibit", closet.echoes)
                            continuation.resume()
                        case unknown => throw new RuntimeException("Unknown Response %s" format unknown)
                }
            }
            continuation.undispatch()
        })
    }

    @RequestMapping(value = Array("/friends"), method = Array(RequestMethod.GET))
    @ResponseBody
    def friends(
            @CookieValue(value = "echoedUserId", required = false) echoedUserIdCookie: String,
            @RequestParam(value = "echoedUserId", required = false) echoedUserIdParam: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val echoedUserId = if (echoedUserIdCookie != null) echoedUserIdCookie else echoedUserIdParam

        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if (continuation.isExpired) {
            logger.error("Request expired to view friends for user {}", echoedUserId)
            new ModelAndView(errorView)
        } else Option(continuation.getAttribute("friends")).getOrElse({
            continuation.suspend(httpServletResponse)

            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onResult {
                case LocateWithIdResponse(_, Left(error)) =>
                case LocateWithIdResponse(_, Right(echoedUserService)) =>
                    echoedUserService.getFriends.onResult{
                        case GetEchoedFriendsResponse(_,Left(error)) =>
                        case GetEchoedFriendsResponse(_,Right(friends)) =>
                            continuation.setAttribute("friends", friends)
                            continuation.resume()
                        case unknown =>    throw new RuntimeException("Unknown Response %s" format unknown)
                }
            }
            continuation.undispatch()
        })
    }
    
    @RequestMapping(value= Array("/exhibit/{id}"), method=Array(RequestMethod.GET))
    @ResponseBody
    def friendExhibit(
            @PathVariable(value="id") echoedFriendId: String,
            @CookieValue(value = "echoedUserId", required= false ) echoedUserId: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        logger.debug("echoedFriendId: {}", echoedFriendId)
        val continuation = ContinuationSupport.getContinuation(httpServletRequest)
        if(continuation.isExpired) {
            
        } else Option(continuation.getAttribute("closet")).getOrElse({
            continuation.suspend(httpServletResponse)
            
            echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onResult {
                case LocateWithIdResponse(_,Left(error)) =>
                case LocateWithIdResponse(_,Right(echoedUserService)) =>
                    echoedUserService.getFriendCloset(echoedFriendId).onResult{
                        case GetFriendExhibitResponse(_,Left(error)) =>
                        case GetFriendExhibitResponse(_,Right(closet)) =>
                            continuation.setAttribute("closet",closet.echoes)
                            continuation.resume()
                    }
            }
            
            continuation.undispatch()
        })
        
        

    }
}
