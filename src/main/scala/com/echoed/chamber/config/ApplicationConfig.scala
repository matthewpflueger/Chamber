package com.echoed.chamber.config

import org.springframework.context.annotation.{Bean, Configuration}
import akka.actor.{Props, ActorSystem}
import javax.annotation.Resource
import com.echoed.chamber.services.GlobalsManager
import com.echoed.chamber.dao._
import com.echoed.chamber.services.geolocation.GeoLocationServiceActor
import com.echoed.util.{ApplicationContextRef, Encrypter, BlobStore}
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
import com.echoed.chamber.services.partner.bigcommerce._
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
import java.util.Properties
import scala.collection.JavaConversions._

@Configuration
class ApplicationConfig {

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

    @Resource(name = "httpClient") var httpClient: Http = _

    @Resource(name = "facebookPostDao") var facebookPostDao: FacebookPostDao = _
    @Resource(name = "facebookLikeDao") var facebookLikeDao: FacebookLikeDao = _
    @Resource(name = "facebookCommentDao") var facebookCommentDao: FacebookCommentDao = _
    @Resource(name = "facebookAccess") var facebookAccess: FacebookAccess = _

    @Resource(name = "cacheManager") var cacheManager: CacheManager = _
    @Resource(name = "facebookUserDao") var facebookUserDao: FacebookUserDao = _
    @Resource(name = "facebookFriendDao") var facebookFriendDao: FacebookFriendDao = _
    @Resource(name = "partnerSettingsDao") var partnerSettingsDao: PartnerSettingsDao = _
    @Resource(name = "urlsProperties") var urlsProperties: Properties = _

    @Resource(name = "networkSolutionsProperties") var networkSolutionsProperties: Properties = _

    @Resource(name = "shopifyProperties") var shopifyProperties: Properties = _

    @Resource(name = "echoDao") var echoDao: EchoDao = _
    @Resource(name = "echoMetricsDao") var echoMetricsDao: EchoMetricsDao = _
    @Resource(name = "echoClickDao") var echoClickDao: EchoClickDao = _
    @Resource(name = "imageService") var imageService: ImageService = _
    @Resource(name = "transactionTemplate") var transactionTemplate: TransactionTemplate = _

    @Resource(name = "twitterAccessProperties") var twitterProperties: Properties = _

    @Resource(name = "twitterAccess") var twitterAccess: TwitterAccess = _
    @Resource(name = "twitterUserDao") var twitterUserDao: TwitterUserDao = _
    @Resource(name = "twitterStatusDao") var twitterStatusDao: TwitterStatusDao = _

    @Resource(name = "closetDao") var closetDao: ClosetDao = _
    @Resource(name = "echoedFriendDao") var echoedFriendDao: EchoedFriendDao = _
    @Resource(name = "storyDao") var storyDao: StoryDao = _
    @Resource(name = "chapterDao") var chapterDao: ChapterDao = _
    @Resource(name = "chapterImageDao") var chapterImageDao: ChapterImageDao = _
    @Resource(name = "commentDao") var commentDao: CommentDao = _
    @Resource(name = "facebookServiceLocator") var facebookServiceLocator: FacebookServiceLocator = _
    @Resource(name = "twitterServiceLocator") var twitterServiceLocator: TwitterServiceLocator = _

    @Resource(name = "encrypter") var encrypter: Encrypter = _
    @Resource(name = "partnerUserDao") var partnerUserDao: PartnerUserDao = _
    @Resource(name = "partnerViewDao") var partnerViewDao: PartnerViewDao = _

    @Resource(name = "adminUserDao") var adminUserDao: AdminUserDao = _
    @Resource(name = "adminViewDao") var adminViewDao: AdminViewDao = _

    @Resource(name = "emailService") var emailService: EmailService = _

    @Resource(name = "shopifyPartnerServiceManager") var shopifyPartnerServiceManager: ShopifyPartnerServiceManager = _
    @Resource(name = "networkSolutionsPartnerServiceManager") var networkSolutionsPartnerServiceManager: NetworkSolutionsPartnerServiceManager = _
    @Resource(name = "bigCommercePartnerServiceManager") var bigCommercePartnerServiceManager: BigCommercePartnerServiceManager = _
    @Resource(name = "magentoGoPartnerServiceManager") var magentoGoPartnerServiceManager: MagentoGoPartnerServiceManager = _

    @Resource(name = "shopifyAccess") var shopifyAccess: ShopifyAccess = _
    @Resource(name = "shopifyPartnerDao") var shopifyPartnerDao: ShopifyPartnerDao = _
    @Resource(name = "partnerServiceManager") var partnerServiceManager: PartnerServiceManager = _

    @Resource(name = "networkSolutionsAccess") var networkSolutionsAccess: NetworkSolutionsAccess = _
    @Resource(name = "networkSolutionsPartnerDao") var networkSolutionsPartnerDao: NetworkSolutionsPartnerDao = _

    @Resource(name = "bigCommerceAccess") var bigCommerceAccess: BigCommerceAccess = _
    @Resource(name = "bigCommercePartnerDao") var bigCommercePartnerDao: BigCommercePartnerDao = _


    @Resource(name = "magentoGoAccess") var magentoGoAccess: MagentoGoAccess = _
    @Resource(name = "magentoGoPartnerDao") var magentoGoPartnerDao: MagentoGoPartnerDao = _


    @Bean(destroyMethod = "shutdown") def actorSystem = ActorSystem("Chamber")

    @Bean def geoLocationService = actorSystem.actorOf(Props(new GeoLocationServiceActor(
            geoLocationDao,
            geoLocationProperties("geoLocationServiceUrl"),
            Integer.parseInt(geoLocationProperties("lastUpdatedBeforeHours")))), "GeoLocationService")

    @Bean def imageServiceActor = actorSystem.actorOf(Props(new ImageServiceActor(
            blobStore,
            imageDao)), "ImageService")

    @Bean def eventServiceActor() = actorSystem.actorOf(Props(new EventServiceActor(eventLogDao)), "EventService")

    @Bean def emailServiceActor() = actorSystem.actorOf(Props(new EmailServiceActor(
            javaMailSender = javaMailSender,
            mustacheEngine = mustacheEngine,
            globalsManager = globalsManager,
            from = mailProperties("mail.from"))), "EmailService")

    @Bean def feedServiceActor = actorSystem.actorOf(Props(new FeedServiceActor(
            feedDao,
            partnerDao,
            echoedUserDao)), "FeedService")

    @Bean
    def facebookAccessActor = actorSystem.actorOf(Props(new FacebookAccessDispatchActor(
            clientId = facebookAccessProperties("clientId"),
            clientSecret = facebookAccessProperties("clientSecret"),
            redirectUrl = facebookAccessProperties("redirectUrl"),
            canvasApp = facebookAccessProperties("canvasApp"),
            appNameSpace = facebookAccessProperties("appNameSpace"),
            httpClient = httpClient)), "FacebookAccess")

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
            urlsProperties("echoClickUrl"))), "FacebookServiceManager")

    @Bean def magentoGoAccessActor = actorSystem.actorOf(Props(new MagentoGoAccessActor(
            httpClient,
            cacheManager)), "MagentoGoAccess")

    @Bean def bigCommerceAccessActor = actorSystem.actorOf(Props(new BigCommerceAccessActor(httpClient)), "BigCommerceAccess")

    @Bean def networkSolutionsAccessActor = actorSystem.actorOf(Props(new NetworkSolutionsDispatchAccessActor(
            networkSolutionsProperties("application"),
            networkSolutionsProperties("certificate"),
            httpClient)), "NetworkSolutionsAccess")

    @Bean def shopifyAccessActor = actorSystem.actorOf(Props(new ShopifyAccessDispatchActor(
            shopifyProperties("shopifyApiKey"),
            shopifyProperties("shopifySecret"),
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
            twitterProperties("consumerKey"),
            twitterProperties("consumerSecret"),
            twitterProperties("callbackUrl"),
            cacheManager = cacheManager)), "TwitterAccess")

    @Bean def twitterServiceLocatorActor = actorSystem.actorOf(Props(new TwitterServiceLocatorActor(
            cacheManager = cacheManager,
            twitterAccess = twitterAccess,
            twitterUserDao = twitterUserDao,
            twitterStatusDao = twitterStatusDao,
            echoClickUrl = urlsProperties("echoClickUrl"))), "TwitterServiceManager")

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
            cacheManager = cacheManager,
            storyGraphUrl = urlsProperties("storyGraphUrl"))), "EchoedUserService")

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
            cacheManager = cacheManager)), "PartnerServiceManager")

    @Bean def shopifyPartnerServiceManagerActor = {
        val sa = Option(shopifyAccess).getOrElse(ctx.getBean("shopifyAccess", classOf[ShopifyAccess]))
        val spd = Option(shopifyPartnerDao).getOrElse(ctx.getBean("shopifyPartnerDao", classOf[ShopifyPartnerDao]))
        actorSystem.actorOf(Props(new ShopifyPartnerServiceManagerActor(
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
            shopifyAccess = sa,
            shopifyPartnerDao = spd,
            accountManagerEmail = mailProperties("accountManagerEmail"))), "ShopifyPartnerServiceManager")
    }

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
            accountManagerEmail = mailProperties("accountManagerEmail"),
            networkSolutionsAccess = networkSolutionsAccess,
            networkSolutionsPartnerDao = networkSolutionsPartnerDao)), "NetworkSolutionsPartnerServiceManager")

    @Bean def bigCommercePartnerServiceManagerActor = {
        val ba = Option(bigCommerceAccess).getOrElse(ctx.getBean("bigCommerceAccess", classOf[BigCommerceAccess]))
        val bcpd = Option(bigCommercePartnerDao).getOrElse(ctx.getBean("bigCommercePartnerDao", classOf[BigCommercePartnerDao]))
        actorSystem.actorOf(Props(new BigCommercePartnerServiceManagerActor(
            bigCommerceAccess = ba,
            bigCommercePartnerDao = bcpd,
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
            accountManagerEmail = mailProperties("accountManagerEmail"))), "BigCommercePartnerServiceManager")
    }

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
            accountManagerEmail = mailProperties("accountManagerEmail"),
            magentoGoAccess = magentoGoAccess,
            magentoGoPartnerDao = magentoGoPartnerDao,
            partnerServiceManager = partnerServiceManager)), "MagentoGoPartnerServiceManager")

}
