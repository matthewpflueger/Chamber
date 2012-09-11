package com.echoed.chamber.services.echoeduser.story


import com.echoed.chamber.services._
import org.springframework.transaction.TransactionStatus
import com.echoed.chamber.domain._
import com.echoed.chamber.dao.partner.{PartnerDao, PartnerSettingsDao}
import com.echoed.chamber.dao._
import com.echoed.chamber.services.echoeduser._
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.util.TransactionUtils._
import scalaz._
import Scalaz._
import com.echoed.chamber.dao.views.FeedDao
import com.echoed.util.ScalaObjectMapper
import java.util.Date
import com.echoed.chamber.services.echoeduser.NewCommentResponse
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.domain.ChapterInfo
import com.echoed.chamber.services.echoeduser.CreateStory
import com.echoed.chamber.services.echoeduser.UpdateStoryResponse
import com.echoed.chamber.domain.Story
import com.echoed.chamber.domain.partner.{PartnerSettings, StoryPrompts, Partner}
import com.echoed.chamber.services.echoeduser.TagStoryResponse
import com.echoed.chamber.services.echoeduser.CreateStoryResponse
import com.echoed.chamber.domain.Chapter
import com.echoed.chamber.services.tag.TagReplaced
import com.echoed.chamber.services.echoeduser.UpdateStory
import com.echoed.chamber.services.echoeduser.StoryUpdated
import com.echoed.chamber.services.echoeduser.CreateChapter
import scala.Right
import com.echoed.chamber.services.echoeduser.TagStory
import com.echoed.chamber.services.echoeduser.CreateChapterResponse
import com.echoed.chamber.domain.ChapterImage
import com.echoed.chamber.domain.views.StoryFull
import com.echoed.chamber.services.echoeduser.NewComment
import com.echoed.chamber.services.echoeduser.UpdateChapterResponse
import com.echoed.chamber.services.echoeduser.UpdateChapter
import com.echoed.chamber.services.partner.{RequestStoryResponseEnvelope, RequestStoryResponse, PartnerClientCredentials, RequestStory}
import akka.pattern._


class StoryService(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        initMessage: Message,
        echoedUser: EchoedUser,
        echoDao: EchoDao,
        storyDao: StoryDao,
        chapterDao: ChapterDao,
        chapterImageDao: ChapterImageDao,
        commentDao: CommentDao,
        imageDao: ImageDao,
        feedDao: FeedDao,
        transactionTemplate: TransactionTemplate) extends OnlineOfflineService {

    private var story: Option[Story] = None
    private var storyFull: Option[StoryFull] = None
    private var partner: Partner = _
    private var partnerSettings: PartnerSettings = _
    private var storyPrompts: StoryPrompts = _
    private var echo: Option[Echo] = None


    private def requestStory(partnerId: String): Unit =
        mp(RequestStory(PartnerClientCredentials(partnerId))).pipeTo(self)


    private def readStoryForEcho(echoId: String) {
        echo = Option(echoDao.findByIdAndEchoedUserId(echoId, echoedUser.id))
        storyFull = echo.map(e => feedDao.findStoryByEchoId(e.id))
        story = storyFull.map(_.story)

        story.foreach(context.parent ! RegisterStory(_))
        requestStory(echo.get.partnerId)
    }

    private def createStoryFull: Unit = storyFull = story.map { s =>
        val chapters = chapterDao.findByStoryId(s.id)
        val chapterImages = chapterImageDao.findByStoryId(s.id)
        val comments = commentDao.findByStoryId(s.id)
        StoryFull(s.id, s, echoedUser, chapters, chapterImages, comments)
    }

    override def preStart() {
        super.preStart()
        initMessage match {
            case msg @ InitStory(_, Some(storyId), _, _) => //mp(ReadStory(storyId)).pipeTo(self)
                story = Option(storyDao.findByIdAndEchoedUserId(storyId, echoedUser.id))

                echo = story
                        .map(s => s.echoId)
                        .map(e => echoDao.findByIdAndEchoedUserId(e , echoedUser.id))

                createStoryFull

                context.parent ! RegisterStory(story.get)
                requestStory(story.get.partnerId)

            case msg @ InitStory(_, _, Some(echoId), _) => readStoryForEcho(echoId)
            case msg @ InitStory(_, _, _, partnerId) => requestStory(partnerId.getOrElse("Echoed"))
            case msg @ CreateStory(_, _, _, Some(echoId), _, _) => readStoryForEcho(echoId)
            case msg @ CreateStory(_, _, _, _, partnerId, _) => requestStory(partnerId.getOrElse("Echoed"))
        }
    }

    def init = {
        case RequestStoryResponse(_, Right(RequestStoryResponseEnvelope(p, ps))) =>
            partner = p
            partnerSettings = ps
            storyPrompts = ps.makeStoryPrompts
            becomeOnline
    }

    def online = {
        case msg: InitStory =>
            sender ! InitStoryResponse(msg, Right(StoryInfo(echoedUser, echo.orNull, partner, storyPrompts, storyFull.orNull)))

        case msg: CreateStory if (story.isDefined) =>
            sender ! CreateStoryResponse(msg, Right(story.get))

        case msg @ CreateStory(_, title, imageId, _, _, productInfo) =>
            val image = imageDao.findById(imageId)
            story = Option(new Story(echoedUser, partner, partnerSettings, image, title, echo, productInfo))
            storyDao.insert(story.get)
            createStoryFull
            ep(StoryUpdated(story.get.id))

            context.parent ! RegisterStory(story.get)
            sender ! CreateStoryResponse(msg, Right(story.get))

        case msg @ UpdateStory(_, storyId, title, imageId) =>
            val story = storyDao
                    .findByIdAndEchoedUserId(storyId, echoedUser.id)
                    .copy(title = title, image = imageDao.findById(imageId))
            storyDao.update(story)
            ep(StoryUpdated(story.id))
            createStoryFull
            sender ! UpdateStoryResponse(msg, Right(story))


        case msg @ TagStory(_, storyId, tagId) =>
            val story = storyDao.findByIdAndEchoedUserId(storyId, echoedUser.id)
            val originalTag = story.tag
            val newStory = story.copy(tag = tagId)
            storyDao.update(newStory)
            ep(TagReplaced(originalTag, tagId) )
            ep(StoryUpdated(storyId))
            createStoryFull
            sender ! TagStoryResponse(msg, Right(newStory))


        case msg @ CreateChapter(eucc, storyId, title, text, imageIds) =>
            val story = storyDao.findByIdAndEchoedUserId(storyId, echoedUser.id)
            val chapter = new Chapter(story, title, text)
            val chapterImages = imageIds.cata(
                ids => ids.map(id => new ChapterImage(chapter, imageDao.findById(id))),
                Array[ChapterImage]())

            transactionTemplate.execute { status: TransactionStatus =>
                chapterDao.insert(chapter)
                chapterImages.foreach(chapterImageDao.insert(_))
                storyDao.update(story) //Update the story to get new timestamp
            }

            ep(StoryUpdated(storyId))
            createStoryFull
            sender ! CreateChapterResponse(msg, Right(ChapterInfo(chapter, chapterImages)))


        case msg @ UpdateChapter(_, storyId, chapterId, title, text, imageIds) =>
            val chapter = chapterDao
                    .findByIdAndEchoedUserId(chapterId, echoedUser.id)
                    .copy(title = title, text = text)

            val chapterImages = imageIds.cata(
                ids => ids.map(id => new ChapterImage(chapter, imageDao.findById(id))),
                Array[ChapterImage]())
            transactionTemplate.execute { status: TransactionStatus =>
                chapterDao.update(chapter)
                chapterImageDao.deleteByChapterId(chapter.id)
                chapterImages.foreach(chapterImageDao.insert(_))
            }
            ep(StoryUpdated(chapter.storyId))
            createStoryFull
            sender ! UpdateChapterResponse(msg, Right(ChapterInfo(chapter, chapterImages)))


        case msg @ NewComment(eucc, byEchoedUser, storyId, chapterId, text, parentCommentId) =>
            val story = storyDao.findByIdAndEchoedUserId(storyId, echoedUser.id)
            assert(story != null, "Did not find story %s for EchoedUser %s" format(storyId, echoedUser.id))

            val comment = new com.echoed.chamber.domain.Comment(
                chapterDao.findByIdAndStoryId(chapterId, storyId),
                byEchoedUser,
                text,
                parentCommentId.map(commentDao.findByIdAndChapterId(_, chapterId)))
            commentDao.insert(comment)
            ep(StoryUpdated(storyId))
            createStoryFull
            sender ! NewCommentResponse(msg, Right(comment))

            if (echoedUser.id != byEchoedUser.id) {
                mp(RegisterNotification(EchoedUserClientCredentials(echoedUser.id), new Notification(
                    echoedUser,
                    byEchoedUser,
                    "comment",
                    Map(
                        "subject" -> byEchoedUser.name,
                        "action" -> "commented on",
                        "object" -> story.title,
                        "storyId" -> storyId))))

            }
    }

}
