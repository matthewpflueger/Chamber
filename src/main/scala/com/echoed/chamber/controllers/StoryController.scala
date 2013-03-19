package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller

import scala.reflect.BeanProperty

import org.springframework.web.bind.annotation._
import com.echoed.chamber.services.echoeduser._
import org.springframework.web.context.request.async.DeferredResult
import com.echoed.chamber.services.echoeduser.CreateChapterResponse
import scala.Right
import com.echoed.chamber.services.echoeduser.CreateStoryResponse
import com.echoed.chamber.services.partneruser.PartnerUserClientCredentials
import javax.annotation.Nullable
import com.echoed.chamber.services.adminuser.AdminUserClientCredentials
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import com.echoed.chamber.domain.{DomainObject, Image, Link, ModerationDescription, Comment, ChapterInfo, Chapter, Story, StoryInfo}
import scala.concurrent.ExecutionContext.Implicits.global
import com.echoed.util.UUID
import java.util.Date
import org.squeryl.annotations.Transient
import java.net.URLEncoder


@Controller
@RequestMapping(Array("/story"))
class StoryController extends EchoedController {

    @RequestMapping(method = Array(RequestMethod.GET))
    @ResponseBody
    def initStory(
            @RequestParam(value = "storyId", required = false) storyId: String,
            @RequestParam(value = "echoId", required = false) echoId: String,
            @RequestParam(value = "partnerId", required = false) partnerId: String,
            @RequestParam(value = "topicId", required = false) topicId: String,
            @RequestParam(value = "contentType", required = false) contentType: String,
            @RequestParam(value = "contentPath", required = false) contentPath: String,
            @RequestParam(value = "contentPageTitle", required = false) contentPageTitle: String,
            eucc: EchoedUserClientCredentials) = {

        log.debug("Initializing story for {}", eucc.id)

        val result = new DeferredResult[StoryInfo](null, ErrorResult.timeout)

        mp(InitStory(
                eucc,
                Option(storyId),
                Option(echoId),
                Option(partnerId),
                Option(topicId),
                Option(contentType),
                Option(contentPath),
                Option(contentPageTitle))).onSuccess {
            case InitStoryResponse(_, Right(storyInfo)) =>
                log.debug("Successfully initialized story for {}", eucc)
                result.setResult(storyInfo)
        }

        result
    }


    @RequestMapping(method = Array(RequestMethod.POST))
    @ResponseBody
    def createStory(
            @RequestParam(value = "storyId", required = true) storyId: String,
            @RequestParam(value = "title", required = false) title: String,
            @RequestParam(value = "imageId", required = false) imageId: String,
            @RequestParam(value = "partnerId", required = false) partnerId: String,
            @RequestParam(value = "productInfo", required = false) productInfo: String,
            @RequestParam(value = "community", required = false) community: String,
            @RequestParam(value = "echoId", required = false) echoId: String,
            @RequestParam(value = "topicId", required = false) topicId: String,
            @RequestParam(value = "contentType", required = false) contentType: String,
            @RequestParam(value = "contentPath", required = false) contentPath: String,
            eucc: EchoedUserClientCredentials) = {

        log.debug("Making story {} for {}", title, eucc)

        val result = new DeferredResult[Story](null, ErrorResult.timeout)

            mp(CreateStory(
                eucc,
                storyId,
                Option(title),
                Option(imageId),
                Option(partnerId),
                Option(productInfo),
                Option(community),
                Option(echoId),
                Option(topicId),
                Option(contentType),
                Option(contentPath))).onSuccess {
            case CreateStoryResponse(_, Right(story)) =>
                log.debug("Successfully made story {} for {}", title, eucc)
                result.setResult(story)
        }

        result
    }

    @RequestMapping(value = Array("/{storyId}"), method = Array(RequestMethod.PUT))
    @ResponseBody
    def updateStory(
            @PathVariable(value = "storyId") storyId: String,
            @RequestParam(value = "title", required = false) title: String,
            @RequestParam(value = "imageId", required = false) imageId: String,
            @RequestParam(value = "productInfo", required = false) productInfo: String,
            @RequestParam(value = "community", required = false) community: String,
            eucc: EchoedUserClientCredentials) = {

        log.debug("Updating story {} for {}", storyId, eucc)

        val result = new DeferredResult[Story](null, ErrorResult.timeout)

        mp(UpdateStory(
                eucc,
                storyId,
                Option(title),
                Option(imageId),
                community,
                Option(productInfo))).onSuccess {
            case UpdateStoryResponse(_, Right(story)) =>
                log.debug("Successfully updated story {} for {}", storyId, eucc)
                result.setResult(story)
        }

        result
    }

    @RequestMapping(value = Array("/{storyId}/community/update"), method = Array(RequestMethod.POST))
    @ResponseBody
    def updateCommunity(
            eucc: EchoedUserClientCredentials,
            @RequestParam(value = "communityId", required = true) communityId: String,
            @PathVariable(value = "storyId") storyId: String) = {

        val result = new DeferredResult[Story](null, ErrorResult.timeout)

        mp(UpdateCommunity(eucc, storyId, communityId)).onSuccess {
            case UpdateCommunityResponse(_, Right(story)) => result.setResult(story)
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

        val result = new DeferredResult[ChapterInfo](null, ErrorResult.timeout)

        mp(CreateStory(
                eucc,
                storyId)).onSuccess {
            case CreateStoryResponse(_, Right(story)) =>
                log.debug("Successfully made story {} for {}", story.title, eucc)

                mp(CreateChapter(
                        eucc,
                        storyId,
                        chapterParams.title,
                        chapterParams.text,
                        Option(chapterParams.imageIds).getOrElse(List.empty[String]),
                        Option(chapterParams.links).getOrElse(List.empty[Link]),
                        Option(chapterParams.publish).map(_.toBoolean))).onSuccess {
                    case CreateChapterResponse(_, Right(chapter)) =>
                        log.debug("Successfully made chapter {} for {}", chapterParams.title, eucc)
                        result.setResult(chapter)
                }
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

        val result = new DeferredResult[ChapterInfo](null, ErrorResult.timeout)

        mp(UpdateChapter(
                eucc,
                storyId,
                chapterId,
                chapterParams.title,
                chapterParams.text,
                Option(chapterParams.imageIds).getOrElse(List.empty[String]),
                Option(chapterParams.links).getOrElse(List.empty[Link]),
                Option(chapterParams.publish).map(_.toBoolean))).onSuccess {
            case UpdateChapterResponse(_, Right(chapter)) =>
                log.debug("Successfully updated chapter {} for {}", chapterId, eucc)
                result.setResult(chapter)
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

        val result = new DeferredResult[Comment](null, ErrorResult.timeout)

        mp(CreateComment(
                credentials,
                storyOwnerId,
                storyId,
                chapterId,
                text,
                Option(parentCommentId))).onSuccess {
            case CreateCommentResponse(_, Right(comment)) =>
                log.debug("Successfully created comment on chapter {} for {}", chapterId, credentials)
                result.setResult(comment)
        }

        result
    }

    @RequestMapping(value = Array("/{storyId}/moderate"), method = Array(RequestMethod.POST))
    @ResponseBody
    def moderateStory(
            @PathVariable("storyId") storyId: String,
            @RequestParam(value = "storyOwnerId", required = true) storyOwnerId: String,
            @RequestParam(value = "moderated", required = false, defaultValue = "true") moderated: Boolean,
            @Nullable aucc: AdminUserClientCredentials,
            @Nullable pucc: PartnerUserClientCredentials,
            @Nullable eucc: EchoedUserClientCredentials) = {

        val result = new DeferredResult[ModerationDescription](null, ErrorResult.timeout)

        val ecc = Option(pucc).orElse(Option(aucc)).orElse(Option(eucc)).get
        mp(ModerateStory(new EchoedUserClientCredentials(storyOwnerId), storyId, ecc, moderated)).onSuccess {
            case ModerateStoryResponse(_, Right(story)) => result.setResult(story)
        }

        result
    }


    @RequestMapping(value = Array("/{storyId}/image"), method = Array(RequestMethod.POST))
    @ResponseBody
    def uploadImage(
            @PathVariable("storyId") storyId: String,
            eucc: EchoedUserClientCredentials,
            request: HttpServletRequest) = {

        val result = new DeferredResult[Map[String, String]](null, ErrorResult.timeout)

        val callback = Option(request.getAttribute("isSecure"))
                .filter(_ == true)
                .map(_ => "%s/%s" format(v.secureSiteUrl, "story/image/callback"))
                .getOrElse("%s/%s" format(v.siteUrl, "story/image/callback"))
        mp(RequestImageUpload(eucc, storyId, callback)).onSuccess {
            case RequestImageUploadResponse(_, Right(params)) => result.setResult(params)
        }

        result
    }

    @RequestMapping(value = Array("/{storyId}/link"), method = Array(RequestMethod.POST))
    @ResponseBody
    def postLink(
            @PathVariable("storyId") storyId: String,
            @RequestParam(value = "url", required = true) url: String,
            eucc: EchoedUserClientCredentials,
            request: HttpServletRequest) = {

        log.debug("Creating link to {} for story {}", url, storyId)

        val result = new DeferredResult[Link](null, ErrorResult.timeout)

        mp(PostLink(eucc, storyId, url)).onSuccess {
            case PostLinkResponse(_, Right(link)) =>
                log.debug("Successfully posted link {} to {} for story {}", link.id, link.url, link.storyId)
                result.setResult(link)
        }

        result
    }

    @RequestMapping(value = Array("/image/callback"), method = Array(RequestMethod.GET))
    def imageCallback = new ModelAndView(v.cloudinaryCallback)
}

case class ChapterParams(
        title: String,
        text: String,
        imageIds: List[String],
        links: List[Link],
        publish: String)
