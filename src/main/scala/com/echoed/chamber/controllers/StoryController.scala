package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller

import scala.reflect.BeanProperty

import org.springframework.web.bind.annotation._
import com.echoed.chamber.services.echoeduser._
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.services.echoeduser.CreateChapterResponse
import scala.Right
import com.echoed.chamber.services.echoeduser.CreateStoryResponse


@Controller
@RequestMapping(Array("/story"))
class StoryController extends EchoedController {

    @RequestMapping(method = Array(RequestMethod.GET))
    @ResponseBody
    def initStory(
            @RequestParam(value = "storyId", required = false) storyId: String,
            @RequestParam(value = "echoId", required = false) echoId: String,
            @RequestParam(value = "partnerId", required = false) partnerId: String,
            eucc: EchoedUserClientCredentials) = {

        log.debug("Initializing story for {}", eucc.echoedUserId)

        val result = new DeferredResult(ErrorResult.timeout)

        mp(InitStory(eucc, Option(storyId), Option(echoId), Option(partnerId))).onSuccess {
            case InitStoryResponse(_, Right(storyInfo)) =>
                log.debug("Successfully initialized story for {}", eucc)
                result.set(storyInfo)
        }

        result
    }


    @RequestMapping(method = Array(RequestMethod.POST))
    @ResponseBody
    def createStory(
            @RequestParam(value = "title", required = true) title: String,
            @RequestParam(value = "imageId", required = true) imageId: String,
            @RequestParam(value = "partnerId", required = false) partnerId: String,
            @RequestParam(value = "echoId", required = false) echoId: String,
            @RequestParam(value = "productInfo", required = false) productInfo: String,
            eucc: EchoedUserClientCredentials) = {

        log.debug("Making story {} for {}", title, eucc)

        val result = new DeferredResult(ErrorResult.timeout)

        mp(CreateStory(
                eucc,
                title,
                imageId,
                Option(partnerId),
                Option(echoId),
                Option(productInfo))).onSuccess {
            case CreateStoryResponse(_, Right(story)) =>
                log.debug("Successfully made story {} for {}", title, eucc)
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
            eucc: EchoedUserClientCredentials) = {

        log.debug("Updating story {} for {}", storyId, eucc)

        val result = new DeferredResult(ErrorResult.timeout)

        mp(UpdateStory(
                eucc,
                storyId,
                title,
                imageId)).onSuccess {
            case UpdateStoryResponse(_, Right(story)) =>
                log.debug("Successfully updated story {} for {}", storyId, eucc)
                result.set(story)
        }

        result
    }

    @RequestMapping(value = Array("/{storyId}/tag"), method = Array(RequestMethod.POST))
    @ResponseBody
    def tagStory(
            @PathVariable("storyId") storyId: String,
            @RequestParam("tagId") tagId: String,
            eucc: EchoedUserClientCredentials) = {

        log.debug("Tagging Story {} with Tag {}", storyId, tagId)

        val result = new DeferredResult(ErrorResult.timeout)

        mp(TagStory(eucc, storyId, tagId)).onSuccess {
            case TagStoryResponse(_ , Right(tag)) =>
                log.debug("Successfully added tag {} to story {}", tagId, storyId)
                result.set(tag)
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
            eucc: EchoedUserClientCredentials) = {

        log.debug("Making chapter {} for {}", chapterParams.title, eucc)

        val result = new DeferredResult(ErrorResult.timeout)

        mp(CreateChapter(
                eucc,
                storyId,
                chapterParams.title,
                chapterParams.text,
                Option(chapterParams.imageIds))).onSuccess {
            case CreateChapterResponse(_, Right(chapter)) =>
                log.debug("Successfully made chapter {} for {}", chapterParams.title, eucc)
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
            eucc: EchoedUserClientCredentials) = {

        log.debug("Updating chapter {} for {}", chapterId, eucc)

        val result = new DeferredResult(ErrorResult.timeout)

        mp(UpdateChapter(
                eucc,
                storyId,
                chapterId,
                chapterParams.title,
                chapterParams.text,
                Option(chapterParams.imageIds))).onSuccess {
            case UpdateChapterResponse(_, Right(chapter)) =>
                log.debug("Successfully updated chapter {} for {}", chapterId, eucc)
                result.set(chapter)
        }

        result
    }

    @RequestMapping(value = Array("/{storyId}/chapter/{chapterId}/comment"), method = Array(RequestMethod.POST))
    @ResponseBody
    def createComment(
            @PathVariable("storyId") storyId: String,
            @PathVariable("chapterId") chapterId: String,
            @RequestParam(value = "storyOwnerId", required = true) storyOwnerId: String,
            @RequestParam(value = "text", required = true) text: String,
            @RequestParam(value = "parentCommentId", required = false) parentCommentId: String,
            credentials: EchoedUserClientCredentials) = {

        log.debug("Creating comment on chapter {} for {}", chapterId, credentials)

        val result = new DeferredResult(ErrorResult.timeout)

        mp(CreateComment(
                credentials,
                storyOwnerId,
                storyId,
                chapterId,
                text,
                Option(parentCommentId))).onSuccess {
            case CreateCommentResponse(_, Right(comment)) =>
                log.debug("Successfully created comment on chapter {} for {}", chapterId, credentials)
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
