package com.echoed.chamber.services.partner

import akka.actor.{Stash, PoisonPill}
import com.echoed.chamber.domain.{Story, Topic}
import com.echoed.chamber.domain.partner.Partner
import com.echoed.chamber.domain.partner.PartnerSettings
import com.echoed.chamber.domain.partner.PartnerUser
import com.echoed.chamber.services._
import com.echoed.chamber.services.echoeduser.{EchoedUserMessageGroup, EchoedUserClientCredentials, FollowPartner, Follower, RegisterNotification, UpdateCustomFeed}
import com.echoed.chamber.services.email.SendEmail
import com.echoed.chamber.services.state._
import com.echoed.util.DateUtils._
import com.echoed.util.{ScalaObjectMapper, Encrypter}
import java.util.{Date, UUID}
import scala.Left
import scala.Right
import scala.Some
import com.echoed.chamber.domain.views.Feed
import com.echoed.chamber.domain.views.context.PartnerContext
import com.echoed.chamber.domain.public.StoryPublic
import com.echoed.util.datastructure.ContentManager
import com.echoed.chamber.domain.views.content.{ContentDescription, Content, PhotoContent}




class PartnerService(
        mp: MessageProcessor,
        ep: EventProcessor,
        encrypter: Encrypter,
        initMessage: Message,
        accountManagerEmail: String = "accountmanager@echoed.com",
        accountManagerEmailTemplate: String = "partner_accountManager_email") extends OnlineOfflineService {

    import context.dispatcher

    protected var partner: Partner = _
    private var partnerSettings: PartnerSettings = _
    private var partnerUser: Option[PartnerUser] = None
    private var topics = List[Topic]()
    private var customization = Map[String, Any]()
    private var followedByUsers = List[Follower]()


    private val contentManager = new ContentManager(List(Story.storyContentDescription, PhotoContent.contentDescription, Story.reviewContentDescription))

    private var contentLoaded = false

    override def preStart() {
        super.preStart()
        initMessage match {
            case msg: PartnerIdentifiable =>
                mp.tell(ReadPartner(msg.credentials), self)
            case msg: RegisterPartner => //handled in init
        }
    }

    private def partnerContext(contentType: ContentDescription) = {
        new PartnerContext(
            partner,
            contentType,
            getStats ::: contentManager.getStats,
            contentManager.getHighlights,
            contentManager.getContentList)
    }

    private def getStats = {
        var stats = List[Map[String, Any]]()
        stats = Map("name" -> "Followers",      "value" -> followedByUsers.length,    "path" -> "followers") :: stats
        stats
    }


    private def becomeOnlineAndRegister {
        becomeOnline
        context.parent ! RegisterPartnerService(partner)
    }

    private def becomeContentLoaded {
        contentLoaded = true
        unstashAll()
    }

    private def getContent {
        mp(FindAllPartnerStories(partner.id)).onSuccess {
            case FindAllPartnerStoriesResponse(_, Right(content)) =>
                val contentList = content.map { c => new StoryPublic(c.asStoryFull.get) }.toList
                self ! InitializePartnerContent(PartnerClientCredentials(partner.id), contentList)
        }
    }

    def init = {
        case msg @ RegisterPartner(userName, email, siteName, siteUrl, shortName, community) =>
            mp.tell(QueryUnique(msg, msg, Option(sender)), self)

        case QueryUniqueResponse(QueryUnique(_, msg: RegisterPartner, Some(channel)), Left(e)) =>
            channel ! RegisterPartnerResponse(msg, Left(InvalidRegistration(e.asErrors())))
            self ! PoisonPill

        case QueryUniqueResponse(QueryUnique(_, msg: RegisterPartner, Some(channel)), Right(true)) =>
            partner = new Partner(msg.siteName, msg.siteUrl, msg.shortName, msg.community).copy(secret = encrypter.generateSecretKey)
            partnerSettings = new PartnerSettings(partner.id, partner.handle)
            customization = partnerSettings.makeCustomizationOptions

            val password = UUID.randomUUID().toString
            partnerUser = Some(new PartnerUser(msg.userName, msg.email)
                    .copy(partnerId = partner.id)
                    .createPassword(password))

            val code = encrypter.encrypt(
                    """{ "password": "%s", "createdOn": "%s" }"""
                    format(password, dateToLong(new Date)))


            channel ! RegisterPartnerResponse(msg, Right(partnerUser.get, partner))

            ep(PartnerCreated(partner, partnerSettings, partnerUser.get))
            becomeOnlineAndRegister

            val model = Map(
                "code" -> code,
                "partner" -> partner,
                "partnerUser" -> partnerUser)

            mp(SendEmail(
                partnerUser.get.email,
                "Your Echoed Account",
                "partner_email_register",
                model))

            mp(SendEmail(
                accountManagerEmail,
                "New partner %s" format partner.name,
                accountManagerEmailTemplate,
                model))

        case msg @ ReadPartnerResponse(_, Left(se)) =>
            partner = new Partner(se.message, se.message)
            partnerSettings = new PartnerSettings(partner.id, partner.domain)

            val password = UUID.randomUUID().toString
            partnerUser = Some(new PartnerUser(partner.domain)
                .copy(partnerId = partner.id)
                .createPassword(password))

            ep(PartnerCreated(partner, partnerSettings, partnerUser.get))
            becomeOnlineAndRegister

        case msg @ ReadPartnerResponse(_, Right(pss)) =>
            partner = pss.partner
            partnerSettings = pss.partnerSettings
            partnerUser = pss.partnerUser
            followedByUsers = pss.followedByUsers
            topics = pss.topics
            customization = pss.partnerSettings.makeCustomizationOptions
            becomeOnlineAndRegister
    }

    private def updateContentManager(content: Content) {
        content match {
            case c: StoryPublic =>
                if(c.isPublished) {
                    contentManager.updateContent(c)
                    c.extractImages.map { i => contentManager.updateContent(new PhotoContent(i, c)) }
                }
            case c: Content =>
                contentManager.updateContent(c)
        }
    }

    def online = {

        case QueryFollowersForPartnerResponse(_, Right(f)) => followedByUsers = followedByUsers ++ f

        case msg: FetchPartner => sender ! FetchPartnerResponse(msg, Right(partner))

        case msg: FetchPartnerAndPartnerSettings =>
            sender ! FetchPartnerAndPartnerSettingsResponse(
                    msg,
                    Right(new PartnerAndPartnerSettings(partner, partnerSettings, customization)))

        case msg @ InitializePartnerContent(_, content) =>
            content.map(updateContentManager(_))
            becomeContentLoaded

        case msg @ ReadAllPartnerContent(_) =>
            if(!contentLoaded) {
                stash()
                getContent
            } else {
                val content = contentManager.getAllContent
                sender ! ReadAllPartnerContentResponse(msg, Right(content))
            }

        case msg @ RequestPartnerContent(_, page, origin, _type) =>
            if(!contentLoaded){
                stash()
                getContent
            } else {
                val content =   contentManager.getContent(_type, page)
                val sf =        new Feed(
                                    partnerContext(_type),
                                    content._1,
                                    content._2)
                sender ! RequestPartnerContentResponse(msg, Right(sf))
            }

        case msg : RequestPartnerFollowers =>
            val feed = new Feed(
                        partnerContext(null),
                        followedByUsers,
                        null)
            sender ! RequestPartnerFollowersResponse(msg, Right(feed))

        case msg: RequestTopics =>
            sender ! RequestTopicsResponse(msg, Right(topics))

        case msg @ RequestStory(_, topicId) =>
            sender ! RequestStoryResponse(msg, Right(RequestStoryResponseEnvelope(
                    partner,
                    partnerSettings,
                    topicId.flatMap(id => topics.find(_.id == id)))))


        case msg @ NotifyPartnerFollowers(_, eucc, n) =>
            val notification = n.copy(origin = partner, notificationType = Some("weekly"))
            var sendFollowRequest = true
            followedByUsers.foreach { f =>
                if (f.echoedUserId == eucc.id) sendFollowRequest = false
                else mp.tell(RegisterNotification(EchoedUserClientCredentials(f.echoedUserId), notification), self)
            }
            if (sendFollowRequest) mp(FollowPartner(eucc, partner.id))

        case msg @ NotifyStoryUpdate(_, s) =>
            if (s.isModerated) contentManager.deleteContent(s)
            else updateContentManager(s)

            val messages = followedByUsers
                    .filterNot(f =>s.isOwnedBy(f.id))
                    .map(f => echoeduser.NotifyStoryUpdate(EchoedUserClientCredentials(f.echoedUserId), s))
            mp.tell(EchoedUserMessageGroup(messages), self)

        case msg @ AddPartnerFollower(_, eu) if (!followedByUsers.exists(_.echoedUserId == eu.id)) =>
            sender ! AddPartnerFollowerResponse(msg, Right(partner))
            followedByUsers = Follower(eu) :: followedByUsers

        case msg @ RemovePartnerFollower(_, eu) if(followedByUsers.exists(_.echoedUserId == eu.id)) =>
            sender ! RemovePartnerFollowerResponse(msg, Right(partner))
            val (_, fbu) = followedByUsers.partition(_.echoedUserId == eu.id)
            followedByUsers = fbu

        case msg @ PutTopic(_, title, description, beginOn, endOn, topicId, community) =>
            try {
                val topic = topicId.flatMap(id => topics.find(_.id == id)).map { t =>
                        t.copy(
                                title = title,
                                description = description.orElse(t.description),
                                beginOn = beginOn.map(dateToLong(_)).getOrElse(t.beginOn),
                                endOn = endOn.map(dateToLong(_)).getOrElse(t.endOn),
                                updatedOn = new Date)
                    }.orElse(Some(new Topic(partner, title, description, beginOn, endOn)))
                    .map(t => t.copy(community = if (partner.isEchoed) community.getOrElse(t.community) else partner.category))
                    .map(t => if (t.beginOn > t.endOn) throw new InvalidDateRange() else t)
                    .map { topic =>
                        topics = if (topic.isUpdated) {
                            ep(TopicUpdated(topic))
                            topics.map(t => if (t.id == topic.id) topic else t)
                        } else {
                            ep(TopicCreated(topic))
                            topic :: topics
                        }
                        topic
                    }.get

                sender ! PutTopicResponse(msg, Right(topic))
            } catch {
                case e: InvalidDateRange => sender ! PutTopicResponse(msg, Left(e))
            }


        case msg @ PutPartnerCustomization(_, customMap) =>
            customMap foreach  { case (key, value) => customization += (key -> value)}
            partnerSettings = partnerSettings.copy(updatedOn = new Date, customization = new ScalaObjectMapper().writeValueAsString(customization))
            ep(PartnerSettingsUpdated(partnerSettings))
            sender ! PutPartnerCustomizationResponse(msg, Right(customization))

    }
}





