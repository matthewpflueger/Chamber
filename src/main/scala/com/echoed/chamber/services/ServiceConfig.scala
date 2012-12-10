package com.echoed.chamber.services

import akka.actor._
import com.echoed.cache.CacheManager
import com.echoed.chamber.LoggingActorSystem
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.services.adminuser.{AdminUserService, AdminUserMessage, AdminUserServiceManager}
import com.echoed.chamber.services.echoeduser.story.StoryService
import com.echoed.chamber.services.echoeduser.{EchoedUserService, EchoedUserMessage, EchoedUserServiceManager}
import com.echoed.chamber.services.email.{SchedulerService, EmailMessage, EmailService}
import com.echoed.chamber.services.event.{EventMessage, EventService}
import com.echoed.chamber.services.facebook._
import com.echoed.chamber.services.feed.{FeedMessage, FeedService}
import com.echoed.chamber.services.partner.{PartnerService, PartnerMessage, PartnerServiceManager}
import com.echoed.chamber.services.partneruser.{PartnerUserService, PartnerUserMessage, PartnerUserServiceManager}
import com.echoed.chamber.services.scheduler.SchedulerMessage
import com.echoed.chamber.services.state.{QueryMessage, QueryService, StateMessage, StateService}
import com.echoed.chamber.services.topic.TopicMessage
import com.echoed.chamber.services.twitter.{TwitterMessage, TwitterAccess}
import com.echoed.util.mustache.MustacheEngine
import com.echoed.util.{ApplicationContextRef, Encrypter}
import java.util.{List => JList, Properties}
import javax.annotation.Resource
import javax.sql.DataSource
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.mail.javamail.JavaMailSender
import scala.collection.mutable.LinkedHashMap
import topic.TopicService


@Configuration
class ServiceConfig {

    private val ctx = ApplicationContextRef.applicationContext

    @Resource(name = "cloudinaryProperties") var cloudinaryProperties: Properties = _

    @Resource(name = "geoLocationProperties") var geoLocationProperties: Properties = _

    @Resource(name = "javaMailSender") var javaMailSender: JavaMailSender = _
    @Resource(name = "globalsManager") var globalsManager: GlobalsManager = _
    @Resource(name = "mustacheEngine") var mustacheEngine: MustacheEngine = _

    @Resource(name = "mailProperties") var mailProperties: Properties = _

    @Resource(name = "facebookAccessProperties") var facebookAccessProperties: Properties = _

    @Resource(name = "cacheManager") var cacheManager: CacheManager = _
    @Resource(name = "urlsProperties") var urlsProperties: Properties = _

    @Resource(name = "networkSolutionsProperties") var networkSolutionsProperties: Properties = _

    @Resource(name = "shopifyProperties") var shopifyProperties: Properties = _

    @Resource(name = "twitterAccessProperties") var twitterProperties: Properties = _

    @Resource(name = "encrypter") var encrypter: Encrypter = _

    @Resource(name = "squerylDataSource") var squerylDataSource: DataSource = _
    @Resource(name = "filteredUserAgents") var filteredUserAgents: JList[String] = _

    @Bean(destroyMethod = "shutdown") def httpClient = dispatch.Http
    @Bean(destroyMethod = "shutdown") def actorSystem = ActorSystem("Chamber")

    @Bean def messageProcessor: MessageProcessor = new MessageProcessorRouter(messageRouter)

    @Bean def eventProcessor = new EventProcessorActorSystem(actorSystem)

    @Bean def log = new LoggingActorSystem(actorSystem)


    @Bean
    def eventService = (ac: ActorContext) => ac.actorOf(Props(new EventService()), "EventService")

    @Bean
    def emailService = (ac: ActorContext) => ac.actorOf(Props(new EmailService(
            javaMailSender = javaMailSender,
            mustacheEngine = mustacheEngine,
            globalsManager = globalsManager,
            from = mailProperties.getProperty("mail.from"),
            development = mailProperties.getProperty("mail.development", "false").toBoolean,
            developmentRecipient = mailProperties.getProperty("mail.developmentRecipient", "developers@echoed.com"))), "EmailService")

    @Bean
    def feedService = (ac: ActorContext) => ac.actorOf(Props(new FeedService(
            messageProcessor,
            eventProcessor)), "FeedService")

    @Bean
    def topicService = (ac: ActorContext) => ac.actorOf(Props(new TopicService(
            messageProcessor,
            eventProcessor)), "TopicService")


    @Bean
    def facebookAccess = (ac: ActorContext) => ac.actorOf(Props(new FacebookAccess(
            clientId = facebookAccessProperties.getProperty("clientId"),
            clientSecret = facebookAccessProperties.getProperty("clientSecret"),
            redirectUrl = facebookAccessProperties.getProperty("redirectUrl"),
            canvasApp = facebookAccessProperties.getProperty("canvasApp"),
            appNameSpace = facebookAccessProperties.getProperty("appNameSpace"),
            httpClient = httpClient)), "FacebookAccess")


    @Bean
    def twitterAccess = (ac: ActorContext) => ac.actorOf(Props(new TwitterAccess(
            twitterProperties.getProperty("consumerKey"),
            twitterProperties.getProperty("consumerSecret"),
            cacheManager = cacheManager)), "TwitterAccess")

    @Bean
    def storyService = (ac: ActorContext, msg: Message, eu: EchoedUser) => ac.actorOf(Props(new StoryService(
            mp = messageProcessor,
            ep = eventProcessor,
            initMessage = msg,
            echoedUser = eu,
            cloudinaryProperties = cloudinaryProperties)))

    @Bean
    def echoedUserService = (ac: ActorContext, msg: Message) => ac.actorOf(Props(new EchoedUserService(
            mp = messageProcessor,
            ep = eventProcessor,
            initMessage = msg,
            storyServiceCreator = storyService,
            storyGraphUrl = urlsProperties.getProperty("storyGraphUrl"),
            encrypter = encrypter,
            echoClickUrl = urlsProperties.getProperty("echoClickUrl"))))

    @Bean
    def echoedUserServiceManager = (ac: ActorContext) => ac.actorOf(Props(new EchoedUserServiceManager(
            mp = messageProcessor,
            ep = eventProcessor,
            facebookAccessCreator = facebookAccess,
            twitterAccessCreator = twitterAccess,
            echoedUserServiceCreator = echoedUserService,
            encrypter = encrypter)), "EchoedUsers")

    @Bean
    def partnerUserService = (ac: ActorContext, msg: Message) => ac.actorOf(Props(new PartnerUserService(
            mp = messageProcessor,
            ep = eventProcessor,
            encrypter = encrypter,
            initMessage = msg)))

    @Bean
    def partnerUserServiceManager = (ac: ActorContext) => ac.actorOf(Props(new PartnerUserServiceManager(
            mp = messageProcessor,
            ep = eventProcessor,
            partnerUserServiceCreator = partnerUserService,
            encrypter = encrypter)), "PartnerUsers")

    @Bean
    def adminUserService = (ac: ActorContext, msg: Message) => ac.actorOf(Props(new AdminUserService(
            mp = messageProcessor,
            ep = eventProcessor,
            initMessage = msg)))

    @Bean
    def adminUserServiceManager = (ac: ActorContext) => ac.actorOf(Props(new AdminUserServiceManager(
            mp = messageProcessor,
            ep = eventProcessor,
            adminUserServiceCreator = adminUserService)), "AdminUsers")


    @Bean
    def partnerService = (ac: ActorContext, msg: Message) => ac.actorOf(Props(new PartnerService(
            mp = messageProcessor,
            ep = eventProcessor,
            initMessage = msg,
            encrypter = encrypter)))


    @Bean
    def partnerServiceManager = (ac: ActorContext) => ac.actorOf(Props(new PartnerServiceManager(
            mp = messageProcessor,
            partnerServiceCreator = partnerService)), "Partners")



    @Bean
    def queryService = (ac: ActorContext) => ac.actorOf(Props(new QueryService(squerylDataSource)), "QueryService")

    @Bean
    def stateService = (ac: ActorContext) => ac.actorOf(Props(new StateService(
            eventProcessor,
            squerylDataSource)), "StateService")

    @Bean
    def schedulerService = (ac: ActorContext) => ac.actorOf(Props(new SchedulerService(
            mp = messageProcessor,
            ep = eventProcessor)), "Scheduler")

    @Bean
    def routeMap = LinkedHashMap[Class[_ <: Message], ActorContext => ActorRef](
            classOf[QueryMessage] -> queryService,
            classOf[StateMessage] -> stateService,
            classOf[SchedulerMessage] -> schedulerService,
            classOf[EventMessage] -> eventService,
            classOf[EmailMessage] -> emailService,
            classOf[FeedMessage] -> feedService,
            classOf[TopicMessage] -> topicService,
            classOf[FacebookMessage] -> facebookAccess,
            classOf[TwitterMessage] -> twitterAccess,
            classOf[EchoedUserMessage] -> echoedUserServiceManager,
            classOf[PartnerUserMessage] -> partnerUserServiceManager,
            classOf[AdminUserMessage] -> adminUserServiceManager,
            classOf[PartnerMessage] -> partnerServiceManager)

    def messageRouter = actorSystem.actorOf(Props.default.withRouter(new MessageRouter(routeMap)), "Services")

}
