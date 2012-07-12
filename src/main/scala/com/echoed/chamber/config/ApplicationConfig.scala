package com.echoed.chamber.config

import org.springframework.context.annotation.{Bean, Configuration}
import akka.actor.{Props, ActorSystem}
import javax.annotation.Resource
import com.echoed.chamber.services.{ActorClient, GlobalsManager}
import com.echoed.chamber.dao._
import com.echoed.chamber.services.geolocation.GeoLocationServiceActor
import org.springframework.beans.factory.annotation.{Value, Autowired}
import com.echoed.util.{Encrypter, BlobStore}
import com.echoed.chamber.services.image.{ImageService, ImageServiceActor}
import com.echoed.chamber.services.event.EventServiceActor
import com.echoed.chamber.services.email.{EmailService, EmailServiceActor}
import org.springframework.mail.javamail.JavaMailSender
import com.echoed.util.mustache.MustacheEngine
import com.echoed.chamber.dao.views.{AdminViewDao, PartnerViewDao, ClosetDao, FeedDao}
import com.echoed.chamber.dao.partner.{PartnerUserDao, PartnerSettingsDao, PartnerDao}
import com.echoed.chamber.services.feed.FeedServiceActor
import dispatch.Http
import com.echoed.chamber.services.facebook._
import com.echoed.cache.CacheManager
import com.echoed.chamber.services.partner.magentogo.{MagentoGoAccess, MagentoGoPartnerServiceManagerActor, MagentoGoPartnerServiceManager, MagentoGoAccessActor}
import com.echoed.chamber.services.partner.bigcommerce.{BigCommercePartnerServiceManagerActor, BigCommerceAccess, BigCommercePartnerServiceManager, BigCommerceAccessActor}
import com.echoed.chamber.services.partner.networksolutions._
import com.echoed.chamber.services.partner.shopify.{ShopifyAccess, ShopifyPartnerServiceManagerActor, ShopifyPartnerServiceManager, ShopifyAccessDispatchActor}
import com.echoed.chamber.services.echo.EchoServiceActor
import org.springframework.transaction.support.TransactionTemplate
import com.echoed.chamber.services.twitter.{TwitterServiceLocator, TwitterAccess, TwitterServiceLocatorActor, TwitterAccessActor}
import com.echoed.chamber.services.echoeduser.EchoedUserServiceLocatorActor
import com.echoed.chamber.services.partneruser.PartnerUserServiceLocatorActor
import com.echoed.chamber.services.adminuser.AdminUserServiceLocatorActor
import com.echoed.chamber.services.partner.{PartnerServiceManager, PartnerServiceManagerActor}
import com.echoed.chamber.dao.partner.shopify.ShopifyPartnerDao
import com.echoed.chamber.dao.partner.networksolutions.NetworkSolutionsPartnerDao
import com.echoed.chamber.dao.partner.bigcommerce.BigCommercePartnerDao
import com.echoed.chamber.dao.partner.magentogo.MagentoGoPartnerDao

@Configuration
class ApplicationConfig {

    @Autowired var geoLocationDao: GeoLocationDao = _
    @Value("${geolocation.serviceUrl}") var geoLocationServiceUrl: String = _
    @Value("${geolocation.lastUpdatedBeforeHours}") var geoLocationLastUpdatedBeforeHours: Int = _

    @Autowired var blobStore: BlobStore = _
    @Autowired var imageDao: ImageDao = _

    @Autowired var eventLogDao: EventLogDao = _

    @Autowired var javaMailSender: JavaMailSender = _
    @Autowired var globalsManager: GlobalsManager = _
    @Autowired var mustacheEngine: MustacheEngine = _
    @Value("${mail.from}") var mailFrom: String = _

    @Autowired var feedDao: FeedDao = _
    @Autowired var partnerDao: PartnerDao = _
    @Autowired var echoedUserDao: EchoedUserDao = _

    @Value("${facebook.clientId}") var facebookClientId: String = _
    @Value("${facebook.clientSecret}") var facebookClientSecret: String = _
    @Value("${facebook.redirectUrl}") var facebookRedirectUrl: String = _
    @Value("${facebook.canvasApp}") var facebookCanvasApp: String = _
    @Value("${facebook.appNameSpace}") var facebookAppNameSpace: String = _

    @Autowired var httpClient: Http = _

    @Autowired var facebookPostDao: FacebookPostDao = _
    @Autowired var facebookLikeDao: FacebookLikeDao = _
    @Autowired var facebookCommentDao: FacebookCommentDao = _
    @Resource(name = "facebookAccess") var facebookAccess: FacebookAccess = _

    @Autowired var cacheManager: CacheManager = _
    @Autowired var facebookUserDao: FacebookUserDao = _
    @Autowired var facebookFriendDao: FacebookFriendDao = _
    @Autowired var partnerSettingsDao: PartnerSettingsDao = _
    @Value("${http.urls.site}/echo") var echoClickUrl: String = _

    @Value("${networksolutions.application}") var networkSolutionsApplication: String = _
    @Value("${networksolutions.certificate}") var networkSolutionsCertificate: String = _

    @Value("${shopify.apiKey}") var shopifyApiKey: String = _
    @Value("${shopify.sharedSecret}") var shopifySharedSecret: String = _

    @Autowired var echoDao: EchoDao = _
    @Autowired var echoMetricsDao: EchoMetricsDao = _
    @Autowired var echoClickDao: EchoClickDao = _
    @Autowired var imageService: ImageService = _
    @Autowired var transactionTemplate: TransactionTemplate = _

    @Value("${twitter.consumerKey}") var twitterConsumerKey: String = _
    @Value("${twitter.consumerSecret}") var twitterConsumerSecret: String = _
    @Value("${twitter.callbackUrl}") var twitterCallbackUrl: String = _

    @Autowired var twitterAccess: TwitterAccess = _
    @Autowired var twitterUserDao: TwitterUserDao = _
    @Autowired var twitterStatusDao: TwitterStatusDao = _

    @Autowired var closetDao: ClosetDao = _
    @Autowired var echoedFriendDao: EchoedFriendDao = _
    @Autowired var storyDao: StoryDao = _
    @Autowired var chapterDao: ChapterDao = _
    @Autowired var chapterImageDao: ChapterImageDao = _
    @Autowired var commentDao: CommentDao = _
    @Autowired var facebookServiceLocator: FacebookServiceLocator = _
    @Autowired var twitterServiceLocator: TwitterServiceLocator = _

    @Autowired var encrypter: Encrypter = _
    @Autowired var partnerUserDao: PartnerUserDao = _
    @Autowired var partnerViewDao: PartnerViewDao = _

    @Autowired var adminUserDao: AdminUserDao = _
    @Autowired var adminViewDao: AdminViewDao = _

    @Autowired var emailService: EmailService = _

    @Autowired var shopifyPartnerServiceManager: ShopifyPartnerServiceManager = _
    @Autowired var networkSolutionsPartnerServiceManager: NetworkSolutionsPartnerServiceManager = _
    @Autowired var bigCommercePartnerServiceManager: BigCommercePartnerServiceManager = _
    @Autowired var magentoGoPartnerServiceManager: MagentoGoPartnerServiceManager = _

    @Autowired var shopifyAccess: ShopifyAccess = _
    @Autowired var shopifyPartnerDao: ShopifyPartnerDao = _
    @Value("${accountManagerEmail}") var accountManagerEmail: String = _
    @Autowired var partnerServiceManager: PartnerServiceManager = _

    @Autowired var networkSolutionsAccess: NetworkSolutionsAccess = _
    @Autowired var networkSolutionsPartnerDao: NetworkSolutionsPartnerDao = _

    @Autowired var bigCommerceAccess: BigCommerceAccess = _
    @Autowired var bigCommercePartnerDao: BigCommercePartnerDao = _

    @Autowired var magentoGoAccess: MagentoGoAccess = _
    @Autowired var magentoGoPartnerDao: MagentoGoPartnerDao = _


    @Bean def cloudPartners = Map(
            "Shopify" -> shopifyPartnerServiceManagerActor,
            "Network Solutions" -> networkSolutionsPartnerServiceManagerActor,
            "BigCommerce" -> bigCommercePartnerServiceManagerActor,
            "MagentoGo" -> magentoGoPartnerServiceManagerActor)


    @Bean(destroyMethod = "shutdown") def actorSystem = ActorSystem("Chamber")

    @Bean def geoLocationService = actorSystem.actorOf(Props(new GeoLocationServiceActor(
            geoLocationDao,
            geoLocationServiceUrl,
            geoLocationLastUpdatedBeforeHours)), "GeoLocationService")

    @Bean def imageServiceActor = actorSystem.actorOf(Props(new ImageServiceActor(
            blobStore,
            imageDao)), "ImageService")

    @Bean def eventServiceActor() = actorSystem.actorOf(Props(new EventServiceActor(eventLogDao)), "EventService")

    @Bean def emailServiceActor() = actorSystem.actorOf(Props(new EmailServiceActor(
            javaMailSender,
            mustacheEngine,
            globalsManager,
            mailFrom)), "EmailService")

    @Bean def feedServiceActor = actorSystem.actorOf(Props(new FeedServiceActor(
            feedDao,
            partnerDao,
            echoedUserDao)), "FeedService")

    @Bean def facebookAccessActor = actorSystem.actorOf(Props(new FacebookAccessDispatchActor(
            facebookClientId,
            facebookClientSecret,
            facebookRedirectUrl,
            facebookCanvasApp,
            facebookAppNameSpace,
            httpClient)), "FacebookAccess")

    @Bean def facebookPostCrawlerActor = actorSystem.actorOf(Props(new FacebookPostCrawlerActor(
            facebookPostDao,
            facebookLikeDao,
            facebookCommentDao,
            facebookAccessActor)), "FacebookPostCrawler")

    @Bean def facebookServiceLocatorActor = actorSystem.actorOf(Props(new FacebookServiceLocatorActor(
            cacheManager,
            facebookAccess,
            facebookUserDao,
            facebookPostDao,
            facebookFriendDao,
            partnerSettingsDao,
            partnerDao,
            echoClickUrl)), "FacebookServiceManager")

    @Bean def magentoGoAccessActor = actorSystem.actorOf(Props(new MagentoGoAccessActor(
            httpClient,
            cacheManager)), "MagentoGoAccess")

    @Bean def bigCommerceAccessActor = actorSystem.actorOf(Props(new BigCommerceAccessActor(httpClient)), "BigCommerceAccess")

    @Bean def networkSolutionsAccessActor = actorSystem.actorOf(Props(new NetworkSolutionsDispatchAccessActor(
            networkSolutionsApplication,
            networkSolutionsCertificate,
            httpClient)), "NetworkSolutionsAccess")

    @Bean def shopifyAccessActor = actorSystem.actorOf(Props(new ShopifyAccessDispatchActor(
            shopifyApiKey,
            shopifySharedSecret,
            httpClient)), "ShopifyAccess")

    @Bean def echoServiceActor = actorSystem.actorOf(Props(new EchoServiceActor(
            partnerDao = partnerDao,
            partnerSettingsDao = partnerSettingsDao,
            echoDao = echoDao,
            echoMetricsDao = echoMetricsDao,
            echoClickDao = echoClickDao,
            imageDao = imageDao,
            imageService = imageService,
            transactionTemplate = transactionTemplate)), "EchoService")

    @Bean def twitterAccessActor = actorSystem.actorOf(Props(new TwitterAccessActor(
            consumerKey = twitterConsumerKey,
            consumerSecret = twitterConsumerSecret,
            callbackUrl = twitterCallbackUrl,
            cacheManager = cacheManager)), "TwitterAccess")

    @Bean def twitterServiceLocatorActor = actorSystem.actorOf(Props(new TwitterServiceLocatorActor(
            cacheManager = cacheManager,
            twitterAccess = twitterAccess,
            twitterUserDao = twitterUserDao,
            twitterStatusDao = twitterStatusDao,
            echoClickUrl = echoClickUrl)), "TwitterServiceManager")

    @Bean def echoedUserServiceLocatorActor = actorSystem.actorOf(Props(new EchoedUserServiceLocatorActor(
            echoedUserDao = echoedUserDao,
            closetDao = closetDao,
            feedDao = feedDao,
            partnerSettingsDao = partnerSettingsDao,
            echoDao = echoDao,
            echoedFriendDao = echoedFriendDao,
            echoMetricsDao = echoMetricsDao,
            partnerDao = partnerDao,
            storyDao = storyDao,
            chapterDao = chapterDao,
            chapterImageDao = chapterImageDao,
            imageDao = imageDao,
            commentDao = commentDao,
            transactionTemplate = transactionTemplate,
            facebookServiceLocator = facebookServiceLocator,
            twitterServiceLocator  = twitterServiceLocator,
            cacheManager = cacheManager)), "EchoedUserService")

    @Bean def partnerUserServiceLocatorActor = actorSystem.actorOf(Props(new PartnerUserServiceLocatorActor(
            cacheManager = cacheManager,
            encrypter = encrypter,
            partnerDao = partnerDao,
            partnerUserDao = partnerUserDao,
            partnerViewDao = partnerViewDao)), "PartnerUserServiceManager")

    @Bean def adminUserServiceLocatorActor = actorSystem.actorOf(Props(new AdminUserServiceLocatorActor(
            cacheManager = cacheManager,
            adminUserDao = adminUserDao,
            adminViewDao = adminViewDao,
            partnerSettingsDao = partnerSettingsDao,
            partnerDao = partnerDao)), "AdminUserServiceManager")

    @Bean def partnerServiceManagerActor = actorSystem.actorOf(Props(new PartnerServiceManagerActor(
            partnerDao = partnerDao,
            partnerSettingsDao = partnerSettingsDao,
            partnerUserDao = partnerUserDao,
            echoDao = echoDao,
            echoMetricsDao = echoMetricsDao,
            imageDao = imageDao,
            imageService = imageService,
            encrypter = encrypter,
            transactionTemplate = transactionTemplate,
            emailService = emailService,
            cacheManager = cacheManager,
            cloudPartners = cloudPartners)), "PartnerServiceManager")

    @Bean def shopifyPartnerServiceManagerActor = actorSystem.actorOf(Props(new ShopifyPartnerServiceManagerActor(
            partnerDao = partnerDao,
            partnerSettingsDao = partnerSettingsDao,
            partnerUserDao = partnerUserDao,
            echoDao = echoDao,
            echoMetricsDao = echoMetricsDao,
            imageDao = imageDao,
            imageService = imageService,
            encrypter = encrypter,
            transactionTemplate = transactionTemplate,
            emailService = emailService,
            cacheManager = cacheManager,
            shopifyAccess = shopifyAccess,
            shopifyPartnerDao = shopifyPartnerDao,
            accountManagerEmail = accountManagerEmail,
            partnerServiceManager = partnerServiceManager)), "ShopifyPartnerServiceManager")

    @Bean def networkSolutionsPartnerServiceManagerActor = actorSystem.actorOf(Props(new NetworkSolutionsPartnerServiceManagerActor(
            partnerDao = partnerDao,
            partnerSettingsDao = partnerSettingsDao,
            partnerUserDao = partnerUserDao,
            echoDao = echoDao,
            echoMetricsDao = echoMetricsDao,
            imageDao = imageDao,
            imageService = imageService,
            encrypter = encrypter,
            transactionTemplate = transactionTemplate,
            emailService = emailService,
            cacheManager = cacheManager,
            accountManagerEmail = accountManagerEmail,
            networkSolutionsAccess = networkSolutionsAccess,
            networkSolutionsPartnerDao = networkSolutionsPartnerDao,
            partnerServiceManager = partnerServiceManager)), "NetworkSolutionsPartnerServiceManager")

    @Bean def bigCommercePartnerServiceManagerActor = actorSystem.actorOf(Props(new BigCommercePartnerServiceManagerActor(
            partnerDao = partnerDao,
            partnerSettingsDao = partnerSettingsDao,
            partnerUserDao = partnerUserDao,
            echoDao = echoDao,
            echoMetricsDao = echoMetricsDao,
            imageDao = imageDao,
            imageService = imageService,
            encrypter = encrypter,
            transactionTemplate = transactionTemplate,
            emailService = emailService,
            cacheManager = cacheManager,
            accountManagerEmail = accountManagerEmail,
            bigCommerceAccess = bigCommerceAccess,
            bigCommercePartnerDao = bigCommercePartnerDao,
            partnerServiceManager = partnerServiceManager)), "BigCommercePartnerServiceManager")
            
    @Bean def magentoGoPartnerServiceManagerActor = actorSystem.actorOf(Props(new MagentoGoPartnerServiceManagerActor(
            partnerDao = partnerDao,
            partnerSettingsDao = partnerSettingsDao,
            partnerUserDao = partnerUserDao,
            echoDao = echoDao,
            echoMetricsDao = echoMetricsDao,
            imageDao = imageDao,
            imageService = imageService,
            encrypter = encrypter,
            transactionTemplate = transactionTemplate,
            emailService = emailService,
            cacheManager = cacheManager,
            accountManagerEmail = accountManagerEmail,
            magentoGoAccess = magentoGoAccess,
            magentoGoPartnerDao = magentoGoPartnerDao,
            partnerServiceManager = partnerServiceManager)), "MagentoGoPartnerServiceManager")

}
