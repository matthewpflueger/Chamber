package com.echoed.chamber.services

import org.springframework.context.annotation.{Bean, Configuration}
import akka.actor._
import javax.annotation.Resource
import com.echoed.chamber.dao._
import com.echoed.chamber.services.geolocation.{GeoLocationMessage, GeoLocationService}
import com.echoed.util.{ApplicationContextRef, Encrypter, BlobStore}
import com.echoed.chamber.services.image.{ImageMessage, ImageService}
import com.echoed.chamber.services.event.{EventMessage, EventService}
import com.echoed.chamber.services.email.{SchedulerService, EmailMessage, EmailService}
import org.springframework.mail.javamail.JavaMailSender
import com.echoed.util.mustache.MustacheEngine
import com.echoed.chamber.dao.views.{AdminViewDao, ClosetDao, FeedDao}
import com.echoed.chamber.dao.partner.{PartnerUserDao, PartnerSettingsDao, PartnerDao}
import com.echoed.chamber.services.feed.{FeedMessage, FeedService}
import dispatch.Http
import com.echoed.chamber.services.facebook._
import com.echoed.cache.CacheManager
import com.echoed.chamber.services.partner.magentogo.{MagentoGoPartnerService, MagentoGoPartnerMessage, MagentoGoAccess, MagentoGoPartnerServiceManager}
import com.echoed.chamber.services.partner.bigcommerce._
import com.echoed.chamber.services.partner.networksolutions._
import com.echoed.chamber.services.partner.shopify.{ShopifyPartnerService, ShopifyPartnerMessage, ShopifyAccess, ShopifyPartnerServiceManager}
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.chamber.services.twitter.{TwitterMessage, TwitterAccess}
import com.echoed.chamber.services.echoeduser.{EchoedUserService, EchoedUserMessage, EchoedUserServiceManager}
import com.echoed.chamber.services.partneruser.{PartnerUserService, PartnerUserMessage, PartnerUserServiceManager}
import com.echoed.chamber.services.adminuser.{AdminUserService, AdminUserMessage, AdminUserServiceManager}
import com.echoed.chamber.services.partner.{PartnerService, PartnerMessage, PartnerServiceManager}
import com.echoed.chamber.dao.partner.shopify.ShopifyPartnerDao
import com.echoed.chamber.dao.partner.networksolutions.NetworkSolutionsPartnerDao
import com.echoed.chamber.dao.partner.bigcommerce.BigCommercePartnerDao
import com.echoed.chamber.dao.partner.magentogo.MagentoGoPartnerDao
import java.util.{List => JList, Properties}
import com.echoed.chamber.LoggingActorSystem
import javax.sql.DataSource
import com.echoed.chamber.services.state.{QueryMessage, QueryService, StateMessage, StateService}
import scala.collection.mutable.LinkedHashMap
import com.echoed.chamber.services.scheduler.SchedulerMessage
import com.echoed.chamber.services.echoeduser.story.StoryService
import com.echoed.chamber.domain.EchoedUser


@Configuration
class ServiceConfig {

    private val ctx = ApplicationContextRef.applicationContext

    @Resource(name = "geoLocationDao") var geoLocationDao: GeoLocationDao = _
    @Resource(name = "geoLocationProperties") var geoLocationProperties: Properties = _

    @Resource(name = "blobStore") var blobStore: BlobStore = _
    @Resource(name = "imageDao") var imageDao: ImageDao = _

    @Resource(name = "eventLogDao") var eventLogDao: EventLogDao = _

    @Resource(name = "javaMailSender") var javaMailSender: JavaMailSender = _
    @Resource(name = "globalsManager") var globalsManager: GlobalsManager = _
    @Resource(name = "mustacheEngine") var mustacheEngine: MustacheEngine = _

    @Resource(name = "mailProperties") var mailProperties: Properties = _

    @Resource(name = "feedDao") var feedDao: FeedDao = _
    @Resource(name = "partnerDao") var partnerDao: PartnerDao = _
    @Resource(name = "echoedUserDao") var echoedUserDao: EchoedUserDao = _

    @Resource(name = "facebookAccessProperties") var facebookAccessProperties: Properties = _

    @Resource(name = "facebookPostDao") var facebookPostDao: FacebookPostDao = _
    @Resource(name = "facebookLikeDao") var facebookLikeDao: FacebookLikeDao = _
    @Resource(name = "facebookCommentDao") var facebookCommentDao: FacebookCommentDao = _

    @Resource(name = "cacheManager") var cacheManager: CacheManager = _
    @Resource(name = "facebookFriendDao") var facebookFriendDao: FacebookFriendDao = _
    @Resource(name = "twitterFollowerDao") var twitterFollowerDao: TwitterFollowerDao = _
    @Resource(name = "partnerSettingsDao") var partnerSettingsDao: PartnerSettingsDao = _
    @Resource(name = "urlsProperties") var urlsProperties: Properties = _

    @Resource(name = "networkSolutionsProperties") var networkSolutionsProperties: Properties = _

    @Resource(name = "shopifyProperties") var shopifyProperties: Properties = _

    @Resource(name = "echoDao") var echoDao: EchoDao = _
    @Resource(name = "echoMetricsDao") var echoMetricsDao: EchoMetricsDao = _
    @Resource(name = "echoClickDao") var echoClickDao: EchoClickDao = _
    @Resource(name = "transactionTemplate") var transactionTemplate: TransactionTemplate = _

    @Resource(name = "twitterAccessProperties") var twitterProperties: Properties = _

    @Resource(name = "twitterStatusDao") var twitterStatusDao: TwitterStatusDao = _

    @Resource(name = "closetDao") var closetDao: ClosetDao = _
    @Resource(name = "echoedFriendDao") var echoedFriendDao: EchoedFriendDao = _

    @Resource(name = "encrypter") var encrypter: Encrypter = _
    @Resource(name = "partnerUserDao") var partnerUserDao: PartnerUserDao = _

    @Resource(name = "adminViewDao") var adminViewDao: AdminViewDao = _

    @Resource(name = "shopifyPartnerDao") var shopifyPartnerDao: ShopifyPartnerDao = _
    @Resource(name = "networkSolutionsPartnerDao") var networkSolutionsPartnerDao: NetworkSolutionsPartnerDao = _
    @Resource(name = "bigCommercePartnerDao") var bigCommercePartnerDao: BigCommercePartnerDao = _
    @Resource(name = "magentoGoPartnerDao") var magentoGoPartnerDao: MagentoGoPartnerDao = _

    @Resource(name = "squerylDataSource") var squerylDataSource: DataSource = _
    @Resource(name = "filteredUserAgents") var filteredUserAgents: JList[String] = _

    @Bean(destroyMethod = "shutdown") def httpClient = dispatch.Http
    @Bean(destroyMethod = "shutdown") def actorSystem = ActorSystem("Chamber")

    @Bean def messageProcessor: MessageProcessor = new MessageProcessorRouter(messageRouter)

    @Bean def eventProcessor = new EventProcessorActorSystem(actorSystem)

    @Bean def log = new LoggingActorSystem(actorSystem)


    @Bean
    def geoLocationService = (ac: ActorContext) => ac.actorOf(Props(new GeoLocationService(
            geoLocationDao,
            geoLocationProperties.getProperty("geoLocationServiceUrl"),
            Integer.parseInt(geoLocationProperties.getProperty("lastUpdatedBeforeHours")))), "GeoLocationService")

    @Bean
    def imageService = (ac: ActorContext) => ac.actorOf(Props(new ImageService(
            blobStore,
            imageDao)), "ImageService")

    @Bean
    def eventService = (ac: ActorContext) => ac.actorOf(Props(new EventService(eventLogDao)), "EventService")

    @Bean
    def emailService = (ac: ActorContext) => ac.actorOf(Props(new EmailService(
            javaMailSender = javaMailSender,
            mustacheEngine = mustacheEngine,
            globalsManager = globalsManager,
            from = mailProperties.getProperty("mail.from"))), "EmailService")

    @Bean
    def feedService = (ac: ActorContext) => ac.actorOf(Props(new FeedService(
            feedDao,
            partnerDao,
            echoedUserDao,
            messageProcessor,
            eventProcessor)), "FeedService")

    @Bean
    def facebookAccess = (ac: ActorContext) => ac.actorOf(Props(new FacebookAccess(
            clientId = facebookAccessProperties.getProperty("clientId"),
            clientSecret = facebookAccessProperties.getProperty("clientSecret"),
            redirectUrl = facebookAccessProperties.getProperty("redirectUrl"),
            canvasApp = facebookAccessProperties.getProperty("canvasApp"),
            appNameSpace = facebookAccessProperties.getProperty("appNameSpace"),
            httpClient = httpClient)), "FacebookAccess")

    @Bean
    def facebookPostCrawler = (ac: ActorContext) => ac.actorOf(Props(new FacebookPostCrawler(
            facebookPostDao,
            facebookLikeDao,
            facebookCommentDao,
            facebookAccess)), "FacebookPostCrawler")


    @Bean
    def magentoGoAccess = (ac: ActorContext) => ac.actorOf(Props(new MagentoGoAccess(
            httpClient,
            cacheManager)), "MagentoGoAccess")

    @Bean
    def bigCommerceAccess = (ac: ActorContext) => ac.actorOf(Props(new BigCommerceAccess(httpClient)), "BigCommerceAccess")

    @Bean
    def networkSolutionsAccess = (ac: ActorContext) => ac.actorOf(Props(new NetworkSolutionsAccess(
            networkSolutionsProperties.getProperty("application"),
            networkSolutionsProperties.getProperty("certificate"),
            httpClient)), "NetworkSolutionsAccess")

    @Bean
    def shopifyAccess = (ac: ActorContext) => ac.actorOf(Props(new ShopifyAccess(
            shopifyProperties.getProperty("shopifyApiKey"),
            shopifyProperties.getProperty("shopifySecret"),
            httpClient)), "ShopifyAccess")

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
            echoedUser = eu)))

    @Bean
    def echoedUserService = (ac: ActorContext, msg: Message) => ac.actorOf(Props(new EchoedUserService(
            mp = messageProcessor,
            ep = eventProcessor,
            initMessage = msg,
            storyServiceCreator = storyService,
            echoedUserDao = echoedUserDao,
            closetDao = closetDao,
            echoedFriendDao = echoedFriendDao,
            feedDao = feedDao,
            partnerSettingsDao = partnerSettingsDao,
            echoDao = echoDao,
            partnerDao = partnerDao,
            echoMetricsDao = echoMetricsDao,
            transactionTemplate = transactionTemplate,
            storyGraphUrl = urlsProperties.getProperty("storyGraphUrl"),
            facebookFriendDao = facebookFriendDao,
            twitterFollowerDao = twitterFollowerDao,
            facebookPostDao = facebookPostDao,
            twitterStatusDao = twitterStatusDao,
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
            initMessage = msg,
            adminViewDao = adminViewDao,
            partnerDao = partnerDao,
            partnerSettingsDao = partnerSettingsDao)))

    @Bean
    def adminUserServiceManager = (ac: ActorContext) => ac.actorOf(Props(new AdminUserServiceManager(
            mp = messageProcessor,
            ep = eventProcessor,
            adminUserServiceCreator = adminUserService)), "AdminUsers")


    @Bean
    def partnerService = (ac: ActorContext, partnerId: String) => ac.actorOf(Props(new PartnerService(
            mp = messageProcessor,
            partnerId = partnerId,
            partnerDao = partnerDao,
            partnerSettingsDao = partnerSettingsDao,
            echoDao = echoDao,
            echoClickDao = echoClickDao,
            echoMetricsDao = echoMetricsDao,
            imageDao = imageDao,
            transactionTemplate = transactionTemplate,
            encrypter = encrypter,
            filteredUserAgents = filteredUserAgents)), partnerId)


    @Bean
    def partnerServiceManager = (ac: ActorContext) => ac.actorOf(Props(new PartnerServiceManager(
            mp = messageProcessor,
            partnerDao = partnerDao,
            partnerSettingsDao = partnerSettingsDao,
            partnerUserDao = partnerUserDao,
            echoDao = echoDao,
            encrypter = encrypter,
            transactionTemplate = transactionTemplate,
            cacheManager = cacheManager,

            partnerServiceCreator = partnerService)), "Partners")


    @Bean
    def shopifyPartnerService = { (ac: ActorContext, partnerId: String) =>
        val spd = Option(shopifyPartnerDao).getOrElse(ctx.getBean("shopifyPartnerDao", classOf[ShopifyPartnerDao]))
        ac.actorOf(Props(new ShopifyPartnerService(
            mp = messageProcessor,
            partnerId = partnerId,
            partnerDao = partnerDao,
            partnerSettingsDao = partnerSettingsDao,
            echoDao = echoDao,
            echoClickDao = echoClickDao,
            echoMetricsDao = echoMetricsDao,
            imageDao = imageDao,
            transactionTemplate = transactionTemplate,
            encrypter = encrypter,
            filteredUserAgents = filteredUserAgents,

            shopifyPartnerDao = spd,
            shopifyAccessCreator = shopifyAccess)), partnerId)
    }


    @Bean
    def shopifyPartnerServiceManager = { ac: ActorContext =>
        val spd = Option(shopifyPartnerDao).getOrElse(ctx.getBean("shopifyPartnerDao", classOf[ShopifyPartnerDao]))
        ac.actorOf(Props(new ShopifyPartnerServiceManager(
            mp = messageProcessor,
            partnerDao = partnerDao,
            partnerSettingsDao = partnerSettingsDao,
            partnerUserDao = partnerUserDao,
            echoDao = echoDao,
            encrypter = encrypter,
            transactionTemplate = transactionTemplate,
            cacheManager = cacheManager,
            shopifyPartnerDao = spd,
            accountManagerEmail = mailProperties.getProperty("accountManagerEmail"),

            shopifyPartnerServiceCreator = shopifyPartnerService,
            shopifyAccessCreator = shopifyAccess)), "ShopifyPartners")
    }


    @Bean
    def networkSolutionsPartnerService = { (ac: ActorContext, partnerId: String) =>
        val nspd = Option(networkSolutionsPartnerDao).getOrElse(ctx.getBean("networkSolutionsPartnerDao", classOf[NetworkSolutionsPartnerDao]))
        ac.actorOf(Props(new NetworkSolutionsPartnerService(
            mp = messageProcessor,
            partnerId = partnerId,
            partnerDao = partnerDao,
            partnerSettingsDao = partnerSettingsDao,
            echoDao = echoDao,
            echoClickDao = echoClickDao,
            echoMetricsDao = echoMetricsDao,
            imageDao = imageDao,
            transactionTemplate = transactionTemplate,
            encrypter = encrypter,
            filteredUserAgents = filteredUserAgents,

            networkSolutionsPartnerDao = nspd,
            networkSolutionsAccessCreator = networkSolutionsAccess)), partnerId)
    }


    @Bean
    def networkSolutionsPartnerServiceManager = { ac: ActorContext =>
        val nspd = Option(networkSolutionsPartnerDao).getOrElse(ctx.getBean("networkSolutionsPartnerDao", classOf[NetworkSolutionsPartnerDao]))
        ac.actorOf(Props(new NetworkSolutionsPartnerServiceManager(
            mp = messageProcessor,
            partnerDao = partnerDao,
            partnerSettingsDao = partnerSettingsDao,
            partnerUserDao = partnerUserDao,
            echoDao = echoDao,
            encrypter = encrypter,
            transactionTemplate = transactionTemplate,
            cacheManager = cacheManager,
            accountManagerEmail = mailProperties.getProperty("accountManagerEmail"),

            networkSolutionsPartnerDao = nspd,
            networkSolutionsPartnerServiceCreator = networkSolutionsPartnerService,
            networkSolutionsAccessCreator = networkSolutionsAccess)), "NetworkSolutionsPartners")
    }


    @Bean
    def bigCommercePartnerService = { (ac: ActorContext, partnerId: String) =>
        val bcpd = Option(bigCommercePartnerDao).getOrElse(ctx.getBean("bigCommercePartnerDao", classOf[BigCommercePartnerDao]))
        ac.actorOf(Props(new BigCommercePartnerService(
            mp = messageProcessor,
            partnerId = partnerId,
            partnerDao = partnerDao,
            partnerSettingsDao = partnerSettingsDao,
            echoDao = echoDao,
            echoClickDao = echoClickDao,
            echoMetricsDao = echoMetricsDao,
            imageDao = imageDao,
            transactionTemplate = transactionTemplate,
            encrypter = encrypter,
            filteredUserAgents = filteredUserAgents,

            bigCommercePartnerDao = bcpd,
            bigCommerceAccessCreator = bigCommerceAccess)), partnerId)
    }


    @Bean
    def bigCommercePartnerServiceManager = { ac: ActorContext =>
        val bcpd = Option(bigCommercePartnerDao).getOrElse(ctx.getBean("bigCommercePartnerDao", classOf[BigCommercePartnerDao]))
        ac.actorOf(Props(new BigCommercePartnerServiceManager(
            mp = messageProcessor,
            partnerDao = partnerDao,
            partnerSettingsDao = partnerSettingsDao,
            partnerUserDao = partnerUserDao,
            echoDao = echoDao,
            encrypter = encrypter,
            transactionTemplate = transactionTemplate,
            cacheManager = cacheManager,
            accountManagerEmail = mailProperties.getProperty("accountManagerEmail"),

            bigCommercePartnerDao = bcpd,
            bigCommercePartnerServiceCreator = bigCommercePartnerService,
            bigCommerceAccessCreator = bigCommerceAccess)), "BigCommercePartners")
    }


    @Bean
    def magentoGoPartnerService = { (ac: ActorContext, partnerId: String) =>
        val mgpd = Option(magentoGoPartnerDao).getOrElse(ctx.getBean("magentoGoPartnerDao", classOf[MagentoGoPartnerDao]))
        ac.actorOf(Props(new MagentoGoPartnerService(
            mp = messageProcessor,
            partnerId = partnerId,
            partnerDao = partnerDao,
            partnerSettingsDao = partnerSettingsDao,
            echoDao = echoDao,
            echoClickDao = echoClickDao,
            echoMetricsDao = echoMetricsDao,
            imageDao = imageDao,
            transactionTemplate = transactionTemplate,
            encrypter = encrypter,
            filteredUserAgents = filteredUserAgents,

            magentoGoPartnerDao = mgpd,
            magentoGoAccessCreator = magentoGoAccess)), partnerId)
    }


    @Bean
    def magentoGoPartnerServiceManager: (ActorContext) => ActorRef = { ac: ActorContext =>
        val mgpd = Option(magentoGoPartnerDao).getOrElse(ctx.getBean("magentoGoPartnerDao", classOf[MagentoGoPartnerDao]))
        ac.actorOf(Props(new MagentoGoPartnerServiceManager(
            mp = messageProcessor,
            partnerDao = partnerDao,
            partnerSettingsDao = partnerSettingsDao,
            partnerUserDao = partnerUserDao,
            echoDao = echoDao,
            encrypter = encrypter,
            transactionTemplate = transactionTemplate,
            cacheManager = cacheManager,
            accountManagerEmail = mailProperties.getProperty("accountManagerEmail"),

            magentoGoPartnerDao = mgpd,
            magentoGoPartnerServiceCreator = magentoGoPartnerService,
            magentoGoAccessCreator = magentoGoAccess)), "MagentoGoPartners")
    }

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
//            classOf[GeoLocationMessage] -> geoLocationService,
            classOf[FacebookPostCrawlerMessage] -> facebookPostCrawler,
            classOf[ImageMessage] -> imageService,
            classOf[EventMessage] -> eventService,
            classOf[EmailMessage] -> emailService,
            classOf[FeedMessage] -> feedService,
            classOf[FacebookMessage] -> facebookAccess,
            classOf[TwitterMessage] -> twitterAccess,
            classOf[EchoedUserMessage] -> echoedUserServiceManager,
            classOf[PartnerUserMessage] -> partnerUserServiceManager,
            classOf[AdminUserMessage] -> adminUserServiceManager,
            classOf[PartnerMessage] -> partnerServiceManager,
            classOf[ShopifyPartnerMessage] -> shopifyPartnerServiceManager,
            classOf[NetworkSolutionsPartnerMessage] -> networkSolutionsPartnerServiceManager,
            classOf[BigCommercePartnerMessage] -> bigCommercePartnerServiceManager,
            classOf[MagentoGoPartnerMessage] -> magentoGoPartnerServiceManager)

    def messageRouter = actorSystem.actorOf(Props.default.withRouter(new MessageRouter(routeMap)), "Services")

}
