package com.echoed.chamber.services.echoeduser.story


import com.echoed.chamber.domain.Chapter
import com.echoed.chamber.domain.ChapterImage
import com.echoed.chamber.domain.ChapterInfo
import com.echoed.chamber.domain.Comment
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.domain.Notification
import com.echoed.chamber.domain.StoryState
import com.echoed.chamber.domain.Vote
import com.echoed.chamber.domain._
import com.echoed.chamber.services._
import com.echoed.chamber.services.adminuser.{AdminUserClientCredentials => AUCC}
import com.echoed.chamber.services.echoeduser._
import com.echoed.chamber.services.echoeduser.{EchoedUserClientCredentials => EUCC}
import com.echoed.chamber.services.partner.NotifyPartnerFollowers
import com.echoed.chamber.services.partner.PartnerClientCredentials
import com.echoed.chamber.services.partner.RequestStory
import com.echoed.chamber.services.partner.RequestStoryResponse
import com.echoed.chamber.services.partner.RequestStoryResponseEnvelope
import com.echoed.chamber.services.partneruser.{PartnerUserClientCredentials => PUCC}
import com.echoed.util.DateUtils._
import com.echoed.util.{ScalaObjectMapper, UUID}
import echoeduser.ChapterCreated
import echoeduser.ChapterUpdated
import echoeduser.CommentCreated
import echoeduser.CreateChapter
import echoeduser.CreateChapterResponse
import echoeduser.CreateStory
import echoeduser.CreateStoryResponse
import echoeduser.EchoedUserClientCredentials
import echoeduser.InitStory
import echoeduser.InitStoryResponse
import echoeduser.ModerateStory
import echoeduser.ModerateStoryResponse
import echoeduser.NewComment
import echoeduser.NewCommentResponse
import echoeduser.NewVote
import echoeduser.NewVoteResponse
import echoeduser.NotifyFollowers
import echoeduser.ProcessImage
import echoeduser.ProcessImageResponse
import echoeduser.RegisterNotification
import echoeduser.RegisterStory
import echoeduser.RequestImageUpload
import echoeduser.RequestImageUploadResponse
import echoeduser.StoryCreated
import echoeduser.StoryImageCreated
import echoeduser.StoryModerated
import echoeduser.StoryUpdated
import echoeduser.StoryViewed
import echoeduser.UpdateChapter
import echoeduser.UpdateChapterResponse
import echoeduser.UpdateCommunity
import echoeduser.UpdateCommunityResponse
import echoeduser.UpdateStory
import echoeduser.UpdateStoryResponse
import echoeduser.VoteCreated
import echoeduser.VoteUpdated
import java.util.{Properties, Date}
import org.apache.commons.codec.digest.DigestUtils
import scala.Left
import scala.Right
import scala.Some
import scala.collection.mutable.{Set => MSet}
import state.ReadStory
import state.ReadStoryForEcho
import state.ReadStoryForEchoResponse
import state.ReadStoryResponse
import state.StoryForEchoNotFound
import state.StoryNotFound
import state._


class StoryService(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem,
        initMessage: Message,
        echoedUser: EchoedUser,
        cloudinaryProperties: Properties) extends OnlineOfflineService {

    import context.dispatcher

    private var storyState: StoryState = _

    private def requestStory(partnerId: Option[String], topicId: Option[String] = None): Unit =
            mp.tell(RequestStory(PartnerClientCredentials(partnerId.getOrElse("Echoed")), topicId), self)

    private def readStory(storyId: String): Unit = mp.tell(ReadStory(storyId), self)

    override def preStart() {
        super.preStart()
        initMessage match {
            case msg @ InitStory(_, Some(storyId), _, _, _) => readStory(storyId)
            case msg @ InitStory(_, _, Some(echoId), _, _) => mp.tell(ReadStoryForEcho(echoId, echoedUser.id), self)
            case msg @ InitStory(_, _, _, partnerId, topicId) => requestStory(partnerId, topicId)
            case msg: StoryIdentifiable => readStory(msg.storyId)
        }
    }

    private def initStory(s: StoryState) {
        storyState = s
        context.parent ! RegisterStory(storyState.asStory)
        becomeOnline
    }

    def init = {
        case ReadStoryResponse(_, Left(StoryNotFound(_, _))) if (initMessage.isInstanceOf[CreateStory]) =>
             requestStory(initMessage.asInstanceOf[CreateStory].partnerId)
        case RequestStoryResponse(_, Right(RequestStoryResponseEnvelope(p, ps, t))) if (initMessage.isInstanceOf[CreateStory]) =>
            initStory(new StoryState(echoedUser, p, ps, topic = t).copy(id = initMessage.asInstanceOf[CreateStory].storyId))

        case ReadStoryResponse(_, Right(s)) => initStory(s)
        case ReadStoryForEchoResponse(_, Right(s)) => initStory(s)
        case ReadStoryForEchoResponse(_, Left(StoryForEchoNotFound(s, _))) => initStory(s)
        case RequestStoryResponse(_, Right(RequestStoryResponseEnvelope(p, ps, t))) =>
            initStory(new StoryState(echoedUser, p, ps, topic = t))
    }

    def online = {
        case msg: InitStory => sender ! InitStoryResponse(msg, Right(storyState.asStoryInfo))
        case msg: CreateStory if (storyState.isCreated) => sender ! CreateStoryResponse(msg, Right(storyState.asStory))

        case msg @ CreateStory(_, storyId, title, imageId, _, productInfo, community, _, topicId) =>
            val image = imageId.map(processImage(_))
            storyState = storyState.create(title, productInfo.orNull, community.orNull, image)
            ep(StoryCreated(storyState))

            sender ! CreateStoryResponse(msg, Right(storyState.asStory))


        case msg @ UpdateStory(_, storyId, title, imageId, community, productInfo) =>
            val image = imageId.map(processImage(_))

            storyState = storyState.copy(
                    title = title,
                    imageId = image.map(_.id).orNull,
                    image = image,
                    community = community,
                    productInfo = productInfo.orNull,
                    updatedOn = new Date)
            ep(new StoryUpdated(storyState))
            sender ! UpdateStoryResponse(msg, Right(storyState.asStory))


        case msg @ NewVote(eucc, byEchoedUser, storyId, value) =>
            val vote = storyState.votes.get(byEchoedUser.id)
                    .map(_.copy(value = value, updatedOn = new Date))
                    .orElse(Option(new Vote("Story", storyId, byEchoedUser.id, value)))
                    .get

            storyState = storyState.copy(votes = storyState.votes + (vote.echoedUserId -> vote), updatedOn = new Date)
            if (vote.isUpdated) ep(VoteUpdated(storyState, vote)) else ep(VoteCreated(storyState, vote))
            if (value > 0 && !vote.isUpdated && (eucc.id != byEchoedUser.id)) {
                mp(RegisterNotification(eucc, new Notification(
                    byEchoedUser,
                    "upvoted",
                    Map(
                        "subject" -> byEchoedUser.name,
                        "action" -> "upvoted",
                        "object" -> storyState.title,
                        "storyId" -> storyState.id))))
            }

            sender ! NewVoteResponse(msg, Right(storyState.asStory))


        case msg @ CreateChapter(eucc, storyId, title, text, imageIds, publish) =>
            val notifyPartnerFollowers = !storyState.isPublished && !storyState.partner.isEchoed
            val publishedOn: Long = if (publish.isEmpty || !publish.get) 0 else new Date

            val chapter = new Chapter(storyState.asStory, title, text).copy(publishedOn = publishedOn)

            val chapterImages = imageIds.map(processImage(_)).map { img =>
                new ChapterImage(chapter, img.id)
            }

            storyState = storyState.copy(
                    chapters = storyState.chapters ::: List(chapter),
                    chapterImages = storyState.chapterImages ::: chapterImages,
                    updatedOn = new Date)

            ep(ChapterCreated(storyState, chapter, chapterImages))
            sender ! CreateChapterResponse(msg, Right(ChapterInfo(chapter, chapterImages)))
            if (publishedOn > 0) notifyFollowersOfStoryUpdate(eucc, notifyPartnerFollowers)


        case msg @ UpdateCommunity(eucc, storyId, communityId) =>
            storyState = storyState.copy(community = communityId)
            ep(StoryUpdated(storyState))
            sender ! UpdateCommunityResponse(msg, Right(storyState.asStory))


        case msg @ UpdateChapter(eucc, storyId, chapterId, title, text, imageIds, publish) =>
            var chapter = storyState.chapters
                    .find(_.id == chapterId)
                    .map(_.copy(title = title, text = text, updatedOn = new Date))
                    .get

            chapter =
                if (!chapter.isPublished && publish.getOrElse(false)) {
                    notifyFollowersOfStoryUpdate(eucc, !storyState.isPublished && !storyState.partner.isEchoed)
                    chapter.copy(publishedOn = new Date)
                } else chapter

            val existingChapterImages = storyState.chapterImages.filter(_.chapterId == chapterId)
            val chapterImages = imageIds.map { id =>
                existingChapterImages.find(ci => id.contains(ci.imageId)).getOrElse {
                    new ChapterImage(chapter, processImage(id).id)
                }
            }

            storyState = storyState.copy(
                    chapters = storyState.chapters.map(c => if (c.id == chapter.id) chapter else c),
                    chapterImages = storyState.chapterImages.filterNot(_.chapterId == chapter.id) ::: chapterImages,
                    updatedOn = new Date)

            ep(ChapterUpdated(storyState, chapter, chapterImages))
            sender ! UpdateChapterResponse(msg, Right(ChapterInfo(chapter, chapterImages)))


        case msg @ NewComment(eucc, byEchoedUser, storyId, chapterId, text, parentCommentId) =>
            val comment = new com.echoed.chamber.domain.Comment(
                storyState.chapters.find(_.id == chapterId).get,
                byEchoedUser,
                text,
                storyState.comments.find(_.id == parentCommentId))

            storyState = storyState.copy(
                    comments = storyState.comments ::: List(comment),
                    updatedOn = new Date)
            ep(CommentCreated(storyState, comment))

            sender ! NewCommentResponse(msg, Right(comment))

            notifyStoryFollowers(new Notification(
                    byEchoedUser,
                    "comment",
                    Map(
                        "subject" -> byEchoedUser.name,
                        "action" -> "commented on",
                        "object" -> storyState.title,
                        "storyId" -> storyState.id)))


        case msg @ ModerateStory(_, _, eucc: EUCC, mo) if (eucc.id == echoedUser.id) =>
            moderate(msg, eucc.name.get, "EchoedUser", eucc.id, mo)
        case msg @ ModerateStory(_, _, pucc: PUCC, mo) if (pucc.partnerId.exists(_ == storyState.partner.id)) =>
            moderate(msg, pucc.name.get, "PartnerUser", pucc.id, mo)
        case msg @ ModerateStory(_, _, aucc: AUCC, mo) => moderate(msg, aucc.name.get, "AdminUser", aucc.id, mo)


        case msg: StoryViewed =>
            //we are not updating the updatedOn on purpose so the FeedService does not push the story to the top :(
            storyState = storyState.copy(views = storyState.views + 1) /*, updatedOn = new Date) */
            ep(StoryUpdated(storyState))


        case msg @ RequestImageUpload(eucc, storyId, callback) =>
            val timestamp = System.currentTimeMillis
            val name = cloudinaryProperties.getProperty("name")
            val apiKey = cloudinaryProperties.getProperty("apiKey")
            val secret = cloudinaryProperties.getProperty("secret")
            val publicId = UUID()
            val tags = "%s,%s" format(eucc.id, storyId)
            val transformation = "a_exif"

            val data = "callback=%s&public_id=%s&tags=%s&timestamp=%s&transformation=%s%s" format(
                    callback,
                    publicId,
                    tags,
                    timestamp,
                    transformation,
                    secret)
            val signature = DigestUtils.shaHex(data)

            sender ! RequestImageUploadResponse(msg, Right(Map(
                    "uploadUrl" -> ("https://api.cloudinary.com/v1_1/%s/upload" format name),
                    "timestamp" -> timestamp.toString,
                    "callback" -> callback,
                    "api_key" -> apiKey,
                    "cloudName" -> name,
                    "public_id" -> publicId,
                    "tags" -> tags,
                    "transformation" -> transformation,
                    "signature" -> signature)))

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

    private def processImage(imageString: String) = {
        if (imageString.startsWith("{")) {
            val m = ScalaObjectMapper(imageString, classOf[Map[String, Any]])
            val image = new Image(
                    m("id").toString,
                    m("url").toString,
                    m("width").asInstanceOf[Int],
                    m("height").asInstanceOf[Int],
                    m("cloudName").toString)
            ep(StoryImageCreated(image))
            self ! ProcessImageResponse(ProcessImage(Left(image)), Right(image))
            image
        } else {
            //throw new UnsupportedOperationException("Image processing no longer supported")
            mp(LookupImage(imageString)).onSuccess {
              case LookupImageResponse(msg, Right(image)) =>
                self ! ProcessImageResponse(ProcessImage(Left(image)), Right(image))
            }
            new Image().copy(id = imageString)
        }
    }

    private def moderate(
            msg: ModerateStory,
            moderatedBy: String,
            moderatedRef: String,
            moderatedRefId: String,
            moderated: Boolean = true) {
        if (moderatedRefId != echoedUser.id || !storyState.isPublished) {
            storyState = storyState.moderate(moderatedBy, moderatedRef, moderatedRefId, moderated)
            ep(StoryModerated(storyState, storyState.moderations.head))
        }
        sender ! ModerateStoryResponse(msg, Right(storyState.moderationDescription))
    }


    private def notifyStoryFollowers(notification: Notification) {
        //anybody that has commented on a Story implicitly follows the Story...
        storyState.comments
            .foldLeft(MSet[String]() += echoedUser.id)((eus: MSet[String], c: Comment) => eus += c.byEchoedUserId)
            .filterNot(_ == notification.origin.id)
            .map(id => mp.tell(RegisterNotification(EUCC(id), notification), self))
    }

    private def notifyFollowersOfStoryUpdate(
            eucc: EchoedUserClientCredentials,
            notifyPartnerFollowers: Boolean = false) {
        val notification = new Notification(
                echoedUser,
                "story updated",
                Map(
                    "subject" -> echoedUser.name,
                    "action" -> "updated story",
                    "object" -> storyState.title,
                    "storyId" -> storyState.id,
                    "partnerId" -> storyState.partner.id,
                    "partnerName" -> storyState.partner.name))
        mp(NotifyFollowers(eucc, notification))
        if (notifyPartnerFollowers)
            mp(NotifyPartnerFollowers(PartnerClientCredentials(storyState.partner.id), eucc, notification))

        notifyStoryFollowers(notification)
    }
}
