package com.echoed.chamber.services.feed

import akka.actor._
import com.echoed.chamber.services._
import collection.immutable.TreeMap
import echoeduser.{StoryEvent, StoryViewed, EchoedUserClientCredentials}
import scala.collection.mutable.HashMap
import akka.pattern._
import com.echoed.chamber.services.state.QueryPartnerIdsResponse
import com.echoed.chamber.domain.views.Feed
import state.FindAllStoriesResponse
import scala.Right
import com.echoed.chamber.services.state.QueryPartnerIds
import com.echoed.chamber.services.state.FindAllStories
import com.echoed.chamber.domain.public.StoryPublic
import com.echoed.chamber.domain.views.context.PublicContext
import com.echoed.util.datastructure.ContentManager
import com.echoed.chamber.domain.Story
import com.echoed.chamber.domain.views.content.{ContentDescription, Content, PhotoContent}


class FeedService(
        mp: MessageProcessor,
        ep: EventProcessorActorSystem) extends EchoedService {

    import context.dispatcher

    private val contentManager = new ContentManager(Content.allContentDescriptions)
    var contentMap = HashMap.empty[String, Content]

    val pageSize = 30

    private def updateContentManager(content: Content) {
        content match {
            case c: StoryPublic =>
                if(c.isPublished && !c.isEchoedModerated && !c.isModerated) {
                    contentManager.updateContent(c)
                    c.extractImages.map { i => contentManager.updateContent(new PhotoContent(i, c)) }
                }
            case c: Content =>
                contentManager.updateContent(c)
        }
        contentMap += (content.id -> content)
    }

    private def publicContext(contentType: ContentDescription) = {
        new PublicContext(
            contentType,
            contentManager.getContentList)
    }

    override def preStart() {
        super.preStart()
        ep.subscribe(context.self, classOf[Event])
        mp.tell(FindAllStories(), self)
    }

    def handle = {
        case msg: StoryEvent =>
            val s = new StoryPublic(msg.story.asStoryFull.get)
            updateContentManager(s)

        case FindAllStoriesResponse(_, Right(all)) => all.map(s => updateContentManager(new StoryPublic(s.asStoryFull.get)))


        case msg @ RequestPublicContent(contentType, page) =>
            val cType =     contentType.getOrElse(contentManager.getDefaultContentType)
            val content =   contentManager.getContent(cType, page)
            val feed =      new Feed(publicContext(cType), content.content, content.nextPage)

            sender ! RequestPublicContentResponse(msg, Right(feed))

        case msg @ GetContent(contentId, origin) =>
            sender ! GetContentResponse(msg, Right(contentMap.get(contentId).map { c =>
                    c match {
                        case s: StoryPublic =>
                            mp(StoryViewed(EchoedUserClientCredentials(s.echoedUser.id), contentId))
                            s.published
                        case content =>
                            content
                    }
            }))

        case msg: GetStoryIds =>
            sender ! GetStoryIdsResponse(msg, Right(contentMap.keySet.toArray))

        case msg: GetPartnerIds =>
            mp(QueryPartnerIds())
                    .mapTo[QueryPartnerIdsResponse]
                    .map(_.resultOrException)
                    .map(r => GetPartnerIdsResponse(msg, Right(r)))
                    .pipeTo(sender)
    }

}
