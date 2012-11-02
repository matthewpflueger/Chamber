package com.echoed.chamber.services.echoeduser.story


import com.echoed.chamber.services._
import com.echoed.chamber.services.echoeduser._
import java.util.Date
import akka.pattern._
import com.echoed.chamber.services.partner._
import com.echoed.chamber.services.echoeduser.{EchoedUserClientCredentials => EUCC}
import com.echoed.chamber.services.partneruser.{PartnerUserClientCredentials => PUCC}
import com.echoed.chamber.services.adminuser.{AdminUserClientCredentials => AUCC}
import com.echoed.util.DateUtils._
import scala.Left
import com.echoed.chamber.domain.ChapterInfo
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.services.image.ProcessImageResponse
import com.echoed.chamber.services.partner.RequestStory
import com.echoed.chamber.services.state.ReadStoryResponse
import scala.Some
import com.echoed.chamber.services.image.ProcessImage
import com.echoed.chamber.domain.Vote
import com.echoed.chamber.domain.StoryState
import com.echoed.chamber.services.state.ReadStoryForEchoResponse
import com.echoed.chamber.services.partner.RequestStoryResponseEnvelope
import com.echoed.chamber.services.partner.PartnerClientCredentials
import com.echoed.chamber.services.state.StoryForEchoNotFound
import com.echoed.chamber.domain.Chapter
import scala.Right
import com.echoed.chamber.domain.Notification
import com.echoed.chamber.domain.ChapterImage
import com.echoed.chamber.services.partner.RequestStoryResponse
import com.echoed.chamber.services.state.ReadStoryForEcho
import com.echoed.chamber.services.state.ReadStory


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
            case msg @ CreateStory(_, _, _, _, _, _, Some(echoId)) => mp(ReadStoryForEcho(echoId, echoedUser.id)).pipeTo(self)
            case msg @ CreateStory(_, _, _, partnerId, _, _, _) => requestStory(partnerId.getOrElse("Echoed"))
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


        case msg @ CreateStory(_, title, imageId, _, productInfo, community, _) =>

            imageId.map(id => mp.tell(ProcessImage(Right(id)), self))

            storyState = storyState.create(title, productInfo.orNull, community.orNull, imageId.orNull)
            ep(StoryCreated(storyState))

            context.parent ! RegisterStory(storyState.asStory)
            sender ! CreateStoryResponse(msg, Right(storyState.asStory))


        case msg @ UpdateStory(_, storyId, title, imageId, community, productInfo) =>

            imageId.map(id => mp.tell(ProcessImage(Right(id)), self))

            storyState = storyState.copy(title = title, imageId = imageId.orNull, image = None, community = community, productInfo = productInfo.orNull, updatedOn = new Date)
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
                    echoedUser.id,
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
            val notifyPartnerFollowers = !storyState.isPublished
            val publishedOn: Long = if (publish.isEmpty || !publish.get) 0 else new Date

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
            if (publishedOn > 0)  notifyFollowersOfStoryUpdate(eucc, notifyPartnerFollowers)


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
                    notifyFollowersOfStoryUpdate(eucc)
                    chapter.copy(publishedOn = new Date)
                } else chapter

            val existingChapterImages = storyState.chapterImages.filter(_.chapterId == chapterId)
            val chapterImages = imageIds.map { id =>
                existingChapterImages.find(_.imageId == id).getOrElse {
                    mp.tell(ProcessImage(Right(id)), self)
                    new ChapterImage(chapter, id)
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

            if (echoedUser.id != byEchoedUser.id) {
                mp(RegisterNotification(eucc, new Notification(
                    echoedUser.id,
                    byEchoedUser,
                    "comment",
                    Map(
                        "subject" -> byEchoedUser.name,
                        "action" -> "commented on",
                        "object" -> storyState.title,
                        "storyId" -> storyState.id))))
            }


        case msg @ ModerateStory(_, _, eucc: EUCC, mo) if (eucc.id == echoedUser.id) =>
            moderate(msg, eucc.name.get, "EchoedUser", eucc.id, mo)
        case msg @ ModerateStory(_, _, pucc: PUCC, mo) if (pucc.partnerId.exists(_ == storyState.partner.id)) =>
            moderate(msg, pucc.name.get, "PartnerUser", pucc.id, mo)
        case msg @ ModerateStory(_, _, aucc: AUCC, mo) => moderate(msg, aucc.name.get, "AdminUser", aucc.id, mo)


        case msg: StoryViewed =>
            //we are not updating the updatedOn on purpose so the FeedService does not push the story to the top :(
            storyState = storyState.copy(views = storyState.views + 1) /*, updatedOn = new Date) */
            ep(StoryUpdated(storyState))


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
        if (moderatedRefId != echoedUser.id || !storyState.isPublished) {
            storyState = storyState.moderate(moderatedBy, moderatedRef, moderatedRefId, moderated)
            ep(StoryModerated(storyState, storyState.moderations.head))
        }
        sender ! ModerateStoryResponse(msg, Right(storyState.moderationDescription))
    }

    private def notifyFollowersOfStoryUpdate(
            eucc: EchoedUserClientCredentials,
            notifyPartnerFollowers: Boolean = false) {
        val notification = new FollowerNotification(
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
    }
}
