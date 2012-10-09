package com.echoed.chamber.services.echoeduser.story


import com.echoed.chamber.services._
import com.echoed.chamber.domain._
import com.echoed.chamber.dao._
import com.echoed.chamber.services.echoeduser._
import org.springframework.transaction.support.TransactionTemplate
import java.util.Date
import akka.pattern._
import com.echoed.chamber.services.state._
import scala.Left
import com.echoed.chamber.services.echoeduser.NewCommentResponse
import com.echoed.chamber.domain.ChapterInfo
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.services.echoeduser.CreateStory
import com.echoed.chamber.services.echoeduser.RegisterStory
import com.echoed.chamber.services.partner.RequestStory
import com.echoed.chamber.services.echoeduser.TagStoryResponse
import com.echoed.chamber.services.echoeduser.CreateStoryResponse
import com.echoed.chamber.services.tag.TagReplaced
import com.echoed.chamber.services.echoeduser.UpdateStory
import com.echoed.chamber.services.echoeduser.InitStoryResponse
import com.echoed.chamber.services.state.ReadStoryResponse
import com.echoed.chamber.services.echoeduser.StoryUpdated
import com.echoed.chamber.services.echoeduser.CreateChapter
import scala.Some
import com.echoed.chamber.services.echoeduser.InitStory
import com.echoed.chamber.services.echoeduser.CreateChapterResponse
import com.echoed.chamber.services.partner.RequestStoryResponseEnvelope
import com.echoed.chamber.services.partner.PartnerClientCredentials
import com.echoed.chamber.services.echoeduser.NewComment
import com.echoed.chamber.services.state.StoryForEchoNotFound
import com.echoed.chamber.services.echoeduser.UpdateChapter
import com.echoed.chamber.services.echoeduser.UpdateStoryResponse
import com.echoed.chamber.domain.Chapter
import scala.Right
import com.echoed.chamber.services.echoeduser.RegisterNotification
import com.echoed.chamber.domain.Notification
import com.echoed.chamber.services.echoeduser.TagStory
import com.echoed.chamber.domain.ChapterImage
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import com.echoed.chamber.services.partner.RequestStoryResponse
import com.echoed.chamber.services.echoeduser.UpdateChapterResponse
import com.echoed.chamber.services.state.ReadStoryForEcho
import com.echoed.chamber.services.state.ReadStory
import com.echoed.util.DateUtils._
import com.echoed.chamber.services.image.{ProcessImage, ProcessImageResponse}


class StoryService(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        initMessage: Message,
        echoedUser: EchoedUser) extends OnlineOfflineService {

    private var storyState: StoryState = _

    private def requestStory(partnerId: String): Unit = mp(RequestStory(PartnerClientCredentials(partnerId))).pipeTo(self)

    override def preStart() {
        super.preStart()
        initMessage match {
            case msg @ InitStory(_, Some(storyId), _, _) => mp(ReadStory(storyId)).pipeTo(self)
            case msg @ InitStory(_, _, Some(echoId), _) => mp(ReadStoryForEcho(echoId, echoedUser.id)).pipeTo(self)
            case msg @ InitStory(_, _, _, partnerId) => requestStory(partnerId.getOrElse("Echoed"))
            case msg @ CreateStory(_, _, _, _, _, Some(echoId)) => mp(ReadStoryForEcho(echoId, echoedUser.id)).pipeTo(self)
            case msg @ CreateStory(_, _, _, partnerId, _, _) => requestStory(partnerId.getOrElse("Echoed"))
            case msg: StoryIdentifiable => mp(ReadStory(msg.storyId)).pipeTo(self)
        }
    }

    private def initStory(s: StoryState) {
        storyState = s
        becomeOnline
    }

    def init = {
        case ReadStoryResponse(_, Right(s)) => initStory(s)
        case ReadStoryForEchoResponse(_, Right(s)) => initStory(s)
        case ReadStoryForEchoResponse(_, Left(StoryForEchoNotFound(s, _))) => initStory(s)
        case RequestStoryResponse(_, Right(RequestStoryResponseEnvelope(p, ps))) => initStory(new StoryState(echoedUser, p, ps))
    }

    def online = {
        case msg: InitStory => sender ! InitStoryResponse(msg, Right(storyState.asStoryInfo))
        case msg: CreateStory if (storyState.isCreated) => sender ! CreateStoryResponse(msg, Right(storyState.asStory))


        case msg @ CreateStory(_, title, imageId, _, productInfo, _) =>

            imageId.map(id => mp.tell(ProcessImage(Right(id)), self))

            storyState = storyState.create(title, productInfo.orNull, imageId.orNull)
            ep(StoryCreated(storyState))

            context.parent ! RegisterStory(storyState.asStory)
            sender ! CreateStoryResponse(msg, Right(storyState.asStory))


        case msg @ UpdateStory(_, storyId, title, imageId, productInfo) =>
            mp.tell(ProcessImage(Right(imageId)), self)
            storyState = storyState.copy(title = title, imageId = imageId, image = None, productInfo = productInfo.orNull, updatedOn = new Date)
            ep(new StoryUpdated(storyState))
            sender ! UpdateStoryResponse(msg, Right(storyState.asStory))

        case msg @ NewVote(eucc, byEchoedUser, storyId, value) =>
            val vote = storyState.votes.get(byEchoedUser.id)
                    .map(_.copy(value = value, updatedOn = new Date))
                    .orElse(Option(new Vote("Story", storyId, byEchoedUser.id, value)))
                    .get

            storyState = storyState.copy(votes = storyState.votes + (vote.echoedUserId -> vote))
            if (vote.isUpdated) ep(VoteUpdated(storyState, vote)) else ep(VoteCreated(storyState, vote))
            if (value > 0 && !vote.isUpdated && (eucc.echoedUserId != byEchoedUser.id)) {
                mp(RegisterNotification(EchoedUserClientCredentials(echoedUser.id), new Notification(
                    echoedUser,
                    byEchoedUser,
                    "comment",
                    Map(
                        "subject" -> byEchoedUser.name,
                        "action" -> "upvoted",
                        "object" -> storyState.title,
                        "storyId" -> storyState.id))))
            }

            sender ! NewVoteResponse(msg, Right(storyState.asStory))

        case msg @ TagStory(_, storyId, tag) =>
            val originalTag = storyState.tag
            storyState = storyState.copy(tag = tag, updatedOn = new Date)
            ep(TagReplaced(originalTag, tag))
            ep(StoryTagged(storyState, originalTag, tag))
            sender ! TagStoryResponse(msg, Right(storyState.asStory))


        case msg @ CreateChapter(_, storyId, title, text, imageIds, publish) =>
            val publishedOn: Long = if(publish.isEmpty || !publish.get) 0 else new Date

            val chapter = new Chapter(storyState.asStory, title, text).copy(publishedOn = publishedOn)

            val chapterImages = imageIds.map { id =>
                mp.tell(ProcessImage(Right(id)), self)
                new ChapterImage(chapter, id)
            }

            storyState = storyState.copy(
                    chapters = storyState.chapters ::: List(chapter),
                    chapterImages = storyState.chapterImages ::: chapterImages,
                    updatedOn = new Date)

            ep(ChapterCreated(storyState, chapter, chapterImages))
            sender ! CreateChapterResponse(msg, Right(ChapterInfo(chapter, chapterImages)))


        case msg @ UpdateChapter(_, storyId, chapterId, title, text, imageIds, publish) =>
            val publishedOn: Long = if(publish.isEmpty || !publish.get) 0 else new Date
            log.debug("Publish On: {} | {}", publish, publishedOn)

            val chapter = storyState.chapters
                    .find(_.id == chapterId)
                    .map(_.copy(title = title, text = text, updatedOn = new Date, publishedOn = publishedOn))
                    .get
            val chapterImages = imageIds.map { id =>
                mp.tell(ProcessImage(Right(id)), self)
                new ChapterImage(chapter, id)
            }

            storyState = storyState.copy(
                    chapters = storyState.chapters.map(c => if (c.id == chapter.id) chapter else c),
                    chapterImages = storyState.chapterImages.filterNot(_.chapterId == chapter.id) ::: chapterImages,
                    updatedOn = new Date)

            ep(ChapterUpdated(storyState, chapter, chapterImages))
            sender ! UpdateChapterResponse(msg, Right(ChapterInfo(chapter, chapterImages)))


        case msg @ NewComment(_, byEchoedUser, storyId, chapterId, text, parentCommentId) =>
            val comment = new com.echoed.chamber.domain.Comment(
                storyState.chapters.find(_.id == chapterId).get,
                byEchoedUser,
                text,
                storyState.comments.find(_.id == parentCommentId))

            storyState = storyState.copy(comments = storyState.comments ::: List(comment), updatedOn = new Date)
            ep(CommentCreated(storyState, comment))

            sender ! NewCommentResponse(msg, Right(comment))

            if (echoedUser.id != byEchoedUser.id) {
                mp(RegisterNotification(EchoedUserClientCredentials(echoedUser.id), new Notification(
                    echoedUser,
                    byEchoedUser,
                    "comment",
                    Map(
                        "subject" -> byEchoedUser.name,
                        "action" -> "commented on",
                        "object" -> storyState.title,
                        "storyId" -> storyState.id))))

            }

        case msg @ ModerateStory(_, _, Left(pucc), mo) => moderate(msg, pucc.name.get, "PartnerUser", pucc.id, mo)
        case msg @ ModerateStory(_, _, Right(aucc), mo) => moderate(msg, aucc.name.get, "AdminUser", aucc.id, mo)

        case msg @ ProcessImageResponse(_, Right(img)) =>
            if (storyState.imageId == img.id) {
                storyState = storyState.copy(image = Option(img), updatedOn = new Date)
                ep(StoryUpdated(storyState))
            } else storyState.chapterImages
                    .find(_.imageId == img.id)
                    .map(_.copy(image = img, updatedOn = new Date))
                    .foreach { chapterImage =>

                storyState.chapters
                        .find(_.id == chapterImage.chapterId)
                        .map(_.copy(updatedOn = new Date))
                        .foreach { chapter =>

                    val chapterImages = storyState.chapterImages.filter(_.chapterId == chapter.id).map { ci =>
                        if (ci.id == chapterImage.id) chapterImage else ci
                    }

                    storyState = storyState.copy(
                            chapters = storyState.chapters.map(c => if (c.id == chapter.id) chapter else c),
                            chapterImages = storyState.chapterImages.filterNot(_.chapterId == chapter.id) ::: chapterImages,
                            updatedOn = new Date)

                    ep(ChapterUpdated(storyState, chapter, chapterImages))
                }
            }

    }

    private def moderate(
            msg: ModerateStory,
            moderatedBy: String,
            moderatedRef: String,
            moderatedRefId: String,
            moderated: Boolean = true) {
        storyState = storyState.moderate(moderatedBy, moderatedRef, moderatedRefId, moderated)
        sender ! ModerateStoryResponse(msg, Right(storyState.moderationDescription.get))
        ep(StoryModerated(storyState, storyState.moderations.head))
    }
}
