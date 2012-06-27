package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller

import scala.reflect.BeanProperty

import org.springframework.web.bind.annotation._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.echoed.chamber.services.echoeduser._
import org.slf4j.LoggerFactory
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.services.echoeduser.CreateChapterResponse
import scala.Right
import com.echoed.chamber.services.echoeduser.CreateStoryResponse
import com.echoed.util.{Encrypter, ScalaObjectMapper}
import org.springframework.web.servlet.ModelAndView


@Controller
@RequestMapping(Array("/story"))
class StoryController {

    private final val logger = LoggerFactory.getLogger(classOf[StoryController])

    @BeanProperty var cookieManager: CookieManager = _
    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var encrypter: Encrypter = _
    @BeanProperty var siteUrl: String = _


    @RequestMapping(value = Array("/code/{code}"), method = Array(RequestMethod.GET))
    def requestStory(
            @PathVariable("code") code: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val result = new DeferredResult(new ModelAndView("redirect:%s" format siteUrl))

        logger.debug("Story request with code {}", code)

        val payload = new ScalaObjectMapper().readTree(encrypter.decrypt(code))

        val echoedUserId = Option(payload.get("echoedUserId")).map(_.asText()).orNull
        val echoId = Option(payload.get("echoId")).map(_.asText()).orNull

        logger.debug("Story request about echo {} for {}", echoId, echoedUserId)

        echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId).onSuccess {
            case LocateWithIdResponse(_, Right(eus)) => eus.getEchoedUser.onSuccess {
                case GetEchoedUserResponse(_, Right(eu)) =>
                    logger.debug("Successful story request about echo {} for {}", echoId, echoedUserId)
                    cookieManager.addEchoedUserCookie(httpServletResponse, eu, httpServletRequest)
                    result.set(new ModelAndView("redirect:%s/#story/%s" format (siteUrl, echoId)))
            }
        }

        result
    }

    @RequestMapping(method = Array(RequestMethod.GET))
    @ResponseBody
    def initStory(
            @RequestParam(value = "storyId", required = false) storyId: String,
            @RequestParam(value = "echoId", required = false) echoId: String,
            @RequestParam(value = "partnerId", required = false) partnerId: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).get

        logger.debug("Initializing story for {}", echoedUserId)

        val result = new DeferredResult("error")

        echoedUserServiceLocator.initStory(echoedUserId, Option(storyId), Option(echoId), Option(partnerId)).onSuccess {
            case InitStoryResponse(_, Right(storyInfo)) =>
                logger.debug("Successfully initialized story for {}", echoedUserId)
                result.set(storyInfo)
        }

        result
    }


    @RequestMapping(method = Array(RequestMethod.POST))
    @ResponseBody
    def createStory(
            @RequestParam(value = "title", required = true) title: String,
            @RequestParam(value = "imageId", required = true) imageId: String,
            @RequestParam(value = "echoId", required = false) echoId: String,
            @RequestParam(value = "productInfo", required = false) productInfo: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).get

        logger.debug("Making story {} for {}", title, echoedUserId)

        val result = new DeferredResult("error")

        echoedUserServiceLocator.createStory(
                echoedUserId,
                title,
                imageId,
                Option(echoId),
                Option(productInfo)).onSuccess {
            case CreateStoryResponse(_, Right(story)) =>
                logger.debug("Successfully made story {} for {}", title, echoedUserId)
                result.set(story)
        }

        result
    }

    @RequestMapping(value = Array("/{storyId}"), method = Array(RequestMethod.PUT))
    @ResponseBody
    def updateStory(
            @PathVariable(value = "storyId") storyId: String,
            @RequestParam(value = "title", required = true) title: String,
            @RequestParam(value = "imageId", required = true) imageId: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).get

        logger.debug("Updating story {} for {}", storyId, echoedUserId)

        val result = new DeferredResult("error")

        echoedUserServiceLocator.updateStory(
                echoedUserId,
                storyId,
                title,
                imageId).onSuccess {
            case UpdateStoryResponse(_, Right(story)) =>
                logger.debug("Successfully updated story {} for {}", storyId, echoedUserId)
                result.set(story)
        }

        result
    }


    @RequestMapping(
            value = Array("/{storyId}/chapter"),
            method = Array(RequestMethod.POST),
            consumes = Array("application/json"))
    @ResponseBody
    def createChapter(
            @PathVariable("storyId") storyId: String,
            @RequestBody chapterParams: ChapterParams,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).get

        logger.debug("Making chapter {} for {}", chapterParams.title, echoedUserId)

        val result = new DeferredResult("error")

        echoedUserServiceLocator.createChapter(
                echoedUserId,
                storyId,
//                chapterParams.storyId,
                chapterParams.title,
                chapterParams.text,
                Option(chapterParams.imageIds)).onSuccess {
            case CreateChapterResponse(_, Right(chapter)) =>
                logger.debug("Successfully made chapter {} for {}", chapterParams.title, echoedUserId)
                result.set(chapter)
        }

        result
    }

    @RequestMapping(
            value = Array("/{storyId}/chapter/{chapterId}"),
            method = Array(RequestMethod.PUT),
            consumes = Array("application/json"))
    @ResponseBody
    def updateChapter(
            @PathVariable("storyId") storyId: String,
            @PathVariable("chapterId") chapterId: String,
            @RequestBody chapterParams: ChapterParams,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).get

        logger.debug("Updating chapter {} for {}", chapterId, echoedUserId)

        val result = new DeferredResult("error")

        echoedUserServiceLocator.updateChapter(
                echoedUserId,
                chapterId,
                chapterParams.title,
                chapterParams.text,
                Option(chapterParams.imageIds)).onSuccess {
            case UpdateChapterResponse(_, Right(chapter)) =>
                logger.debug("Successfully updated chapter {} for {}", chapterId, echoedUserId)
                result.set(chapter)
        }

        result
    }

    @RequestMapping(
            value = Array("/{storyId}/chapter/{chapterId}/comment"),
            method = Array(RequestMethod.POST))
    @ResponseBody
    def createComment(
            @PathVariable("storyId") storyId: String,
            @PathVariable("chapterId") chapterId: String,
            @RequestParam(value = "text", required = true) text: String,
            @RequestParam(value = "parentCommentId", required = false) parentCommentId: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        val echoedUserId = cookieManager.findEchoedUserCookie(httpServletRequest).get

        logger.debug("Creating comment on chapter {} for {}", chapterId, echoedUserId)

        val result = new DeferredResult("error")

        echoedUserServiceLocator.createComment(
                echoedUserId,
                storyId,
                chapterId,
                text,
                Option(parentCommentId)).onSuccess {
            case CreateCommentResponse(_, Right(comment)) =>
                logger.debug("Successfully created comment on chapter {} for {}", chapterId, echoedUserId)
                result.set(comment)
        }

        result
    }
}

class ChapterParams(
            @BeanProperty var title: String,
            @BeanProperty var text: String,
            @BeanProperty var imageIds: Array[String]) {
    def this() = this(null, null, null)
}
