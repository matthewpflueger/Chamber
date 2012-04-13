package com.echoed.chamber.util

import com.echoed.chamber.controllers.EchoPossibilityParameters
import scala.reflect.BeanProperty
import org.springframework.beans.factory.annotation.Autowired
import com.echoed.chamber.domain.views.EchoView
import scala.collection.JavaConversions.asScalaBuffer
import org.codehaus.jackson.`type`.TypeReference
import org.slf4j.LoggerFactory
import com.echoed.chamber.services.facebook.Me
import java.net.{HttpURLConnection, URLEncoder, URL}
import java.io.{InputStreamReader, BufferedReader}
import org.codehaus.jackson.map.ObjectMapper
import scala.collection.mutable.{ListBuffer, Buffer}
import com.echoed.chamber.dao._
import com.echoed.util.ScalaObjectMapper
import com.echoed.chamber.domain._
import java.util.{Properties, Random, List => JList, Calendar, Date, UUID}


class DataCreator {

    @BeanProperty var facebookTestUserDao: FacebookTestUserDao = _
    @BeanProperty var facebookUserDao: FacebookUserDao = _
    @BeanProperty var echoedUserDao: EchoedUserDao = _
    @BeanProperty var retailerDao: RetailerDao = _
    @BeanProperty var retailerSettingsDao: RetailerSettingsDao = _
    @BeanProperty var echoDao: EchoDao = _
    @BeanProperty var echoMetricsDao: EchoMetricsDao = _
    @BeanProperty var retailerUserDao: RetailerUserDao = _

    @BeanProperty var urlsProperties: Properties = _
    @BeanProperty var facebookAccessProperties: Properties = _
    @BeanProperty var twitterAccessProperties: Properties = _




    private val logger = LoggerFactory.getLogger(classOf[DataCreator])

    var imagesUrl = "http://v1-cdn.echoed.com" //okay
    var siteUrl = "http://www.echoed.com" //okay
    var appId = "177687295582534" //okay
    var appAccessToken = "177687295582534|zXC5wmZqodeHhTpUVXThov7zKrA" //okay
    val createTestUserUrl = "https://graph.facebook.com/%s/accounts/test-users?access_token=%s&name=%s&permissions=%s&method=post&installed=%s"
    val allTestUsersUrl = "https://graph.facebook.com/%s/accounts/test-users?access_token=%s" format(appId, appAccessToken)
    val meUrl = "https://graph.facebook.com/me?access_token=%s"
    val changeUrl = "https://graph.facebook.com/%s?password=%s&method=post&access_token=%s"
    val linkUrl = "https://graph.facebook.com/%s/friends/%s?method=post&access_token=%s"
    val addAppUrl = "https://graph.facebook.com/%s/accounts/test-users?installed=true&permissions=email,publish_stream,offline_access&uid=%s&owner_access_token=177687295582534|zXC5wmZqodeHhTpUVXThov7zKrA&access_token=%s&method=post"

    def init() {
        appId = facebookAccessProperties.getProperty("clientId")
        appAccessToken = facebookAccessProperties.getProperty("appAccessToken")
        imagesUrl = urlsProperties.getProperty("http.urls.images")
        siteUrl = urlsProperties.getProperty("http.urls.site")
    }

    def open(url: URL)(op: BufferedReader => Unit) = {
        val reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()))
        try {
            op(reader)
        } finally {
            reader.close
        }
    }

    def createFacebookTestUser(
            permissions:String = "email,publish_stream,offline_access",
            installed: Boolean = true) = {

        val stringBuilder = new StringBuilder(1024)

        open(new URL(createTestUserUrl format(appId, appAccessToken, permissions, installed))){ reader =>
            var line: String = reader.readLine
            while (line != null) {
                stringBuilder.append(line)
                line = reader.readLine
            }

            val content = stringBuilder.toString()
            if (content.contains("Error")) throw new RuntimeException(content)
            else logger.debug("Created {}", content)
            stringBuilder.clear
         }
    }

    def importFacebookTestUsers(
            simpleTestUsers: List[FacebookSimpleTestUser],
            objectMapper: ObjectMapper,
            totalImportedUsers: Int)(preImport: (FacebookTestUser, Int) => FacebookTestUser) = {

        var total = totalImportedUsers

        simpleTestUsers.foreach { simpleTestUser =>
            try {
                total = total + 1
                logger.debug("Working on {}: {}", total, simpleTestUser)
                val me: Me = objectMapper.readValue(new URL(meUrl format(simpleTestUser.access_token)), new TypeReference[Me]() {})
                logger.debug("Got me {}: {}", total, me)
                val testUser = me.createFacebookTestUser(simpleTestUser.login_url, simpleTestUser.access_token)
                val tu = preImport(testUser, total)
                facebookTestUserDao.insertOrUpdate(tu)
                logger.debug("Imported {}: {}", total, tu)
            } catch {
                case e => logger.error("Error importing %s: %s" format(total, simpleTestUser), e)
            }
        }

        total
    }

    def importFacebookTestUsers(
            url: URL,
            objectMapper: ObjectMapper,
            totalImportedUsers: Int)(preImport: (FacebookTestUser, Int) => FacebookTestUser): Int = {

        val allTestUsers: FacebookAllTestUsers = objectMapper.readValue(url, new TypeReference[FacebookAllTestUsers]() {})


        val total = importFacebookTestUsers(
            asScalaBuffer(allTestUsers.data).toList,
            objectMapper,
            totalImportedUsers)(preImport)

        if (allTestUsers.paging.next == null) total
        else {
            logger.debug("Getting next page of test users {}", allTestUsers.paging.next)
            importFacebookTestUsers(new URL(allTestUsers.paging.next), objectMapper, total)(preImport)
        }
    }

    def importFacebookTestUsers() {
        val url = new URL(allTestUsersUrl)
        val objectMapper = new ScalaObjectMapper

        val total = /*importFacebookTestUsers(url, objectMapper, 0)((testUser: FacebookTestUser, number: Int) => {
            try {
                //this calls Facebook and changes the password
                open(new URL(changeUrl format(
                        testUser.facebookId,
                        testUser.password,
                        appAccessToken))) { _ => }
            } catch {
                case e => logger.error("Error changing password for %s: %s" format(number, testUser), e)
            }
            testUser
        })*/

        importFacebookTestUsers(url, objectMapper, 0)((testUser: FacebookTestUser, number: Int) => { testUser })
        logger.debug("Import {} FacebookTestUsers", total)
    }

    def linkFacebookTestUsers() {
        val facebookTestUsers = facebookTestUserDao.selectAll

        facebookTestUsers.foreach { u1 =>
            facebookTestUsers.foreach { u2 =>
                if (u1.id != u2.id) {
                    try {
                        open(new URL(linkUrl format(u1.facebookId, u2.facebookId, u1.accessToken)))({ _ =>
                                logger.debug("Created friend request from {} to {}", u1, u2)})

                        open(new URL(linkUrl format(u2.facebookId, u1.facebookId, u2.accessToken)))({ _ =>
                                logger.debug("Confirmed friend request from {} to {}", u1, u2)})
                    } catch {
                        case e => logger.error("%s and %s could not be friends!" format (u1, u2), e)
                    }
                }
            }
        }
    }

    def addFacebookTestUsersToApps() {
        val appAccessTokens =
            "277490472318816|jzXtF0cEQ2zwpvPvC74EVvzKfxA" ::
            "323745504330890|JiFUfho10x6LUQvT0FVL7ZX4Tm4" ::
            "243792655699913|-5VLcQG_x9LZK1EU9MeN4totMq8" :: Nil

        val facebookTestUsers = facebookTestUserDao.selectAll
        facebookTestUsers.add(facebookTestUser)

        appAccessTokens.foreach { token =>
            val id = token.substring(0, 15)
            logger.debug("Adding Facebook test users to application {}", id)

            facebookTestUsers.foreach { user =>
                open(new URL(addAppUrl format(id, user.facebookId, token)))({ _ =>
                    logger.debug("Added Facebook user {} to app {}", user, id)})
            }
        }
    }


    def cleanupEchoedUsers(list: Buffer[(FacebookUser, EchoedUser)]) {
        list.foreach { tuple =>
            val (fu, eu) = tuple
            facebookUserDao.deleteByEmail(fu.email)
            echoedUserDao.deleteByEmail(eu.email)
        }
    }

    def generateEchoedUsers = {
        val facebookTestUsers = facebookTestUserDao.selectFirst(20).dropWhile { u =>
            u.email == null || u.email == facebookUser.email
        }.zipWithIndex

        logger.debug("Generating data for {} Facebook test users", facebookTestUsers.length)

        val list = Buffer.empty[(FacebookUser, EchoedUser)]

        facebookTestUsers.foreach { tuple =>
            val (ftu, index) = tuple
            logger.debug("Working on {}: {}", index, ftu)

            var fu = ftu.createFacebookUser
            val eu = new EchoedUser(fu)
            fu = fu.copy(echoedUserId = eu.id)

            list += ((fu, eu))
        }

        cleanupEchoedUsers(list)

        list.foreach { tuple =>
            val (fu, eu) = tuple

            facebookUserDao.insertOrUpdate(fu)
            logger.debug("Created {}", fu)

            echoedUserDao.insert(eu)
            logger.debug("Created {}", eu)
        }

        list
    }

    def generateDataSet() {
        logger.debug("Generating data set")

        retailers.foreach { r =>
            retailerSettingsDao.deleteByRetailerId(r.id)
            echoDao.deleteByRetailerId(r.id)
            retailerDao.deleteByName(r.name)

            retailerDao.insert(r)
            logger.debug("Created {}", r)
        }

        retailerSettingsList.foreach { rs =>
            retailerSettingsDao.insert(rs)
            logger.debug("Created {}", rs)
        }

        retailerUserDao.insert(retailerUser)

        val random = new Random
        val users = generateEchoedUsers.map(_._2).zipWithIndex

        users.foreach { tuple =>
            val (eu, index) = tuple

            val ran = random.nextInt(retailers.length)
            logger.debug("Generating {} echoes", ran)

            for (num <- 0 to ran) {
                val r = retailers(num)
                val rs = retailerSettingsList.find(_.retailerId == r.id).get

                val categories = List("Shoes","Clothes","Random","Other","Accessories")
                val category = categories(random.nextInt(5))
                val picNum = random.nextInt(15) + 1
                var e = Echo.make(
                    retailerId = r.id,
                    customerId = "customerId",
                    productId = "productId",
                    boughtOn = new Date,
                    orderId = "orderId",
                    price = random.nextInt(100).toFloat+5,
                    imageUrl = "%s/Pic%s.jpg" format(imagesUrl, picNum),
                    landingPageUrl = siteUrl,
                    productName = "productName",
                    category = category,
                    brand = "brand",
                    description = "These are amazing",
                    step = null,
                    browserId = "browserId",
                    ipAddress = "127.0.0.1",
                    referrerUrl = "http://facebook.com",
                    userAgent = "test",
                    echoClickId = null)
                e = e.copy(
                        echoedUserId = eu.id,
                        echoPossibilityId = "echoPossibilityId%s-%s-%s" format(index, num, picNum),
                        retailerSettingsId = rs.id)


                var em = new EchoMetrics(e, rs)
                e = e.copy(echoMetricsId = em.id)
                em = em.echoed(rs)

                val clicks = random.nextInt(rs.maxClicks)
                logger.debug("Clicking {} times on {}", clicks, e)
                for (clicked <- 0 to random.nextInt(rs.maxClicks)) em = em.clicked(rs)

                echoDao.insert(e)
                echoMetricsDao.insert(em)
                logger.debug("Created {}", e)
            }
        }
    }


    val on = new Date
    val today = Calendar.getInstance()
    val yesterday = {
        val c = Calendar.getInstance()
        c.set(Calendar.DATE, today.get(Calendar.DATE)-1)
        c
    }
    val past = {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, today.get(Calendar.YEAR)-1)
        c
    }
    val future = {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, today.get(Calendar.YEAR)+1)
        c
    }

    val echoedUserId = UUID.randomUUID.toString
    val fromEchoedUserId = echoedUserId
    val toEchoedUserId = UUID.randomUUID.toString
    val retailerUserId = UUID.randomUUID.toString
    val twitterUserId = UUID.randomUUID.toString
    val facebookUserId = UUID.randomUUID.toString
    val facebookTestUserId = UUID.randomUUID.toString
    val retailerId = "e0142506-7d92-4222-b55e-6dd2bca08c93" //UUID.randomUUID.toString
    val adminUserId = UUID.randomUUID.toString

    val landingPageUrl = siteUrl
    val echoImageFileName_1 = "Pic1.jpg"
    val echoImageFileName_2 = "Pic2.jpg"
    val echoImageUrl_1 = "%s/%s" format(imagesUrl, echoImageFileName_1)
    val echoImageUrl_2 = "%s/%s" format(imagesUrl, echoImageFileName_2)


    val retailers = List(
        new Retailer("A Lady and Her Baby").copy(id = retailerId, secret = "wC4nP6DLLtaPji6pWSEcBg"),
        new Retailer("Babesta"),
        new Retailer("Carrot Top"),
        new Retailer("Dimples"),
        new Retailer("Economy Candy"),
        new Retailer("Flight Club"),
        new Retailer("Galt Baby"),
        new Retailer("Halt Pint Citizens"),
        new Retailer("Ina"),
        new Retailer("Ju Ju Beane Boutique"),
        new Retailer("Kirma Zabete"),
        new Retailer("Lemonade Couture"),
        new Retailer("Madison Rose"),
        new Retailer("Neda"),
        new Retailer("Oak"),
        new Retailer("Pink Olive"),
        new Retailer("Salvor Projects")
    )

    //val retailers = retailerDao.selectAll
    val retailer = retailers(0)
    val creditWindow = 168
    val retailerSettingsFuture = RetailerSettings(
            id = UUID.randomUUID.toString,
            updatedOn = on,
            createdOn = on,
            retailerId = retailerId,
            closetPercentage = 0.01f,
            minClicks = 1,
            minPercentage = 0.1f,
            maxClicks = 10,
            maxPercentage = 0.2f,
            echoedMatchPercentage = 1f,
            echoedMaxPercentage = 0.2f,
            activeOn = future.getTime,
            creditWindow = creditWindow)
    val retailerSettingsList = retailerSettingsFuture :: retailers.map { r =>
            new RetailerSettings(
                retailerId = r.id,
                closetPercentage = 0.01f,
                minClicks = 1,
                minPercentage = 0.1f,
                maxClicks = 10,
                maxPercentage = 0.2f,
                echoedMatchPercentage = 1f,
                echoedMaxPercentage = 0.2f,
                activeOn = past.getTime,
                creditWindow = creditWindow)
        }.toList
    val retailerSettings = retailerSettingsList(1)



    val twitterId ="47851866"
    val twitterScreenName = "MisterJWU"
    val twitterPassword = "gateway2"
    val twitterUserProfileImageUrl = "%s/logo.png" format imagesUrl

    val twitterUser = TwitterUser(
        twitterUserId,
        on,
        on,
        echoedUserId,
        twitterId,
        twitterScreenName,
        "Test TwitterUser",
        twitterUserProfileImageUrl,
        "location",
        "timezone",
        "accessToken",
        "accessTokenSecret")

    /*
        curl -v 'https://graph.facebook.com/177687295582534/accounts/test-users?access_token=177687295582534|zXC5wmZqodeHhTpUVXThov7zKrA&name=TestUser&permissions=email,publish_stream,offline_access&method=post&installed=true'

        //OLD {"id":"100003128184602","access_token":"AAAChmwwiYUYBAJG7MomgcAy1ZCg0fEuXBSjM45n80FV0CHofT1VLZCeGp805f5qt6odHkKBMUwB9n75GJZCrzmbc3nZCDUZBpuxT4WyXliQZDZD","login_url":"https:\/\/www.facebook.com\/platform\/test_account_login.php?user_id=100003128184602&n=R0ZipMc3NCuutvb","email":"testuser_jasdmrk_testuser\u0040tfbnw.net","password":"970285973"}
        {"id":"100003177284815","access_token":"AAAChmwwiYUYBAKI2bxTrAgnIgLMok1r8Xel3lgBqu0uqR8RtFaxdzXVEzek7MYNlkIxZB4TXcZCZCZBnzM8auZAWZAZCJLNotEhu1tL24ImxAZDZD","login_url":"https:\/\/www.facebook.com\/platform\/test_account_login.php?user_id=100003177284815&n=8L2tMNJBPGMWlAE","email":"testuser_jpmknrv_testuser\u0040tfbnw.net","password":"273385869"}
    */
    /*
        Old test user (not an actual Facebook test user)
        val testUserFacebookId = "100003076656188"
        val testUserEmail = "tech@echoed.com"
        val testUserPass = "etech25"
     */
    val facebookId = "100003177284815"
    val facebookUserEmail = "testuser_jpmknrv_testuser@tfbnw.net"
    val echoedUserEmail = facebookUserEmail
    val facebookUserPassword = "1234567890"
    val facebookUserLoginPageUrl = "https://www.facebook.com/platform/test_account_login.php?user_id=100003177284815&n=cbCYwnYPx73Nbm4"
    val facebookUserAccessToken = "AAAD8YEkH92ABAORXZCAzEzFRf7kOXqoV9xZCcLWzROESl0p8QmKdZAMFi1wg6WiqrQ7VA5WEq1R4UTkSydNUVnuWRYOZBFT47hReP40a7wZDZD"

    val facebookUser = FacebookUser(
        facebookUserId,
        on,
        on,
        echoedUserId,
        facebookId,
        "Test FacebookUser",
        echoedUserEmail,
        "http://www.facebook.com/profile.php?id=%s" format facebookId,
        "male",
        "-5",
        "en_US",
        facebookUserAccessToken)

    val facebookTestUser = FacebookTestUser(
        facebookTestUserId,
        on,
        on,
        echoedUserId,
        facebookUserId,
        facebookId,
        "Test FacebookTestUser",
        echoedUserEmail,
        facebookUserPassword,
        facebookUserLoginPageUrl,
        facebookUserAccessToken)

    val facebookFriends = List(
        new FacebookFriend(
            facebookUserId,
            facebookId + "1",
            "Test FacebookFriend 1"
        ),
        new FacebookFriend(
            facebookUserId,
            facebookId + "2",
            "Test FacebookFriend 2"
        )
    )

    val twitterFollowerId = "twitterFollowerId"
    val twitterFollowerName = "twitterFollowerName"
    val twitterFollowers = List(
        new TwitterFollower(
            twitterUserId,
            twitterFollowerId,
            twitterFollowerName
        ),
        new TwitterFollower(
            twitterUserId,
            twitterFollowerId,
            twitterFollowerName
        )
    )

    val adminUserPassword = "testpassword"
    val adminUser = new AdminUser(
        adminUserId,
        "Jonathan",
        "tech@echoed.com"
    ).createPassword(adminUserPassword)

    val retailerUserPassword = "testpassword"
    val retailerUser = new RetailerUser(
        retailerId,
        "Test PartnerUser",
        "tech@echoed.com"
    ).createPassword(retailerUserPassword)

    val echoedUser = EchoedUser(
        echoedUserId,
        on,
        on,
        "Test User",
        echoedUserEmail,
        twitterUser.screenName,
        facebookUser.id,
        facebookUser.facebookId,
        twitterUser.id,
        twitterUser.twitterId)

    val fromEchoedUser = echoedUser

    val toEchoedUser = EchoedUser(
        toEchoedUserId,
        on,
        on,
        "Test User",
        echoedUserEmail,
        twitterUser.screenName,
        facebookUser.id,
        facebookUser.facebookId,
        twitterUser.id,
        twitterUser.twitterId)

    val echoedFriends = List(
        new EchoedFriend(
            echoedUser.id,
            toEchoedUser.id,
            "Test Friend",
            "TestFriend",
            facebookUser.id,
            facebookUser.facebookId,
            twitterUser.id,
            twitterUser.twitterId),
        new EchoedFriend(
            toEchoedUser.id,
            echoedUser.id,
            "Test Friend",
            "TestFriend",
            facebookUser.id,
            facebookUser.facebookId,
            twitterUser.id,
            twitterUser.twitterId)
    )

    val facebookPostId_1 = UUID.randomUUID.toString
    val facebookPostId_2 = UUID.randomUUID.toString
    val twitterStatusId_1 = UUID.randomUUID.toString
    val twitterStatusId_2 = UUID.randomUUID.toString
    val echoId_1 = UUID.randomUUID.toString
    val echoId_2 = UUID.randomUUID.toString

    val ipAddress_1 = "66.202.133.170"
    val orderId_1 = "orderId_1"
    val productId_1 = "productId_1"
    val customerId_1 = "customerId_1"
    val price_1 = 10.00f
    val productName_1 = "My Awesome Boots"
    val category_1 = "Footwear"
    val brand_1 = "Nike"
    val description_1 = "These are amazing boots"

    val ipAddress_2 = "66.202.133.171"
    val orderId_2 = "orderId_2"
    val productId_2 = "productId_2"
    val customerId_2 = "customerId_2"
    val price_2 = 20.00f
    val productName_2 = "My Awesome Gloves"
    val category_2 = "Accessories"
    val brand_2 = "Reebok"
    val description_2= "These are amazing gloves"

    val ipAddress_3 = "66.202.133.172"
    val ipAddress_4 = "66.202.133.173"

    val echoPossibilities = List(
        EchoPossibilityParameters(
            retailerId,
            customerId_1,
            productId_1,
            on,
            orderId_1,
            price_1,
            echoImageUrl_1,
            echoedUserId,
            echoId_1,
            landingPageUrl,
            productName_1,
            category_1,
            brand_1,
            description_1,
            null).createButtonEchoPossibility,
        EchoPossibilityParameters(
            retailerId,
            customerId_2,
            productId_2,
            on,
            orderId_2,
            price_2,
            echoImageUrl_2,
            echoedUserId,
            echoId_2,
            landingPageUrl,
            productName_2,
            category_2,
            brand_2,
            description_2,
            null).createButtonEchoPossibility
    )

    val twitterStatuses = List(
        TwitterStatus(
            twitterStatusId_1,
            on,
            on,
            echoId_1,
            echoedUserId,
            "message",
            "twitterId",
            new Date,
            "text",
            "source",
            new Date
        ),
        TwitterStatus(
            twitterStatusId_2,
            on,
            on,
            echoId_2,
            echoedUserId,
            "message",
            "twitterId",
            new Date,
            "text",
            "source",
            new Date
        )
    )

    val facebookPosts = List(
        FacebookPost(
            id = facebookPostId_1,
            updatedOn = on,
            createdOn = on,
            name = "name",
            message = "message",
            caption = "caption",
            picture = "picture",
            link = "link",
            facebookUserId = facebookUserId,
            echoedUserId = echoedUserId,
            echoId = echoId_1,
            postedOn = on,
            facebookId = "100003177284815_125947867521122",
            crawledStatus = null,
            crawledOn = null,
            retries = 0),
        FacebookPost(
            id = facebookPostId_2,
            updatedOn = on,
            createdOn = on,
            name="name",
            message = "message",
            caption = "caption",
            picture = "picture",
            link = "link",
            facebookUserId = facebookUserId,
            echoedUserId = echoedUserId,
            echoId = echoId_2,
            postedOn = on,
            facebookId = "100003177284815_125947200854522",
            crawledStatus = null,
            crawledOn = null,
            retries = 0)
    )

    /* Other FacebookPost ids we could use...
        "100003177284815_125946577521251"
        "100003177284815_125945957521313"
        "100003177284815_125945630854679"
        "100003177284815_124951754287400"
        "100003177284815_124951254287450"
        "100003177284815_124950930954149"
    */

    val facebookLikes = List(
        FacebookLike(
            id = UUID.randomUUID().toString,
            updatedOn = on,
            createdOn = on,
            facebookPostId = facebookPostId_1,
            facebookUserId = facebookUserId,
            echoedUserId = echoedUserId,
            facebookId = facebookId,
            name = facebookUser.name),
        FacebookLike(
            id = UUID.randomUUID().toString,
            updatedOn = on,
            createdOn = on,
            facebookPostId = facebookPostId_1,
            facebookUserId = facebookUserId,
            echoedUserId = echoedUserId,
            facebookId = facebookId + "2",
            name = "Test Name"))

    val facebookComments = List(
        FacebookComment(
            id = UUID.randomUUID().toString,
            updatedOn = on,
            createdOn = on,
            facebookPostId = facebookPostId_1,
            facebookUserId = facebookUserId,
            echoedUserId = echoedUserId,
            facebookId = facebookId,
            byFacebookId = "byFacebookId",
            name = "name",
            message = "message",
            createdAt = on),
        FacebookComment(
            id = UUID.randomUUID().toString,
            updatedOn = on,
            createdOn = on,
            facebookPostId = facebookPostId_1,
            facebookUserId = facebookUserId,
            echoedUserId = echoedUserId,
            facebookId = facebookId + "2",
            byFacebookId = "byFacebookId",
            name = "name",
            message = "message",
            createdAt = on))

    val echoClicks_1 = List(
        EchoClick(
            id = UUID.randomUUID.toString,
            updatedOn = on,
            createdOn = on,
            echoId = echoId_1,
            facebookPostId = facebookPostId_1,
            twitterStatusId = null,
            echoedUserId = echoedUserId,
            referrerUrl = "http://facebook.com",
            browserId = UUID.randomUUID().toString,
            ipAddress = ipAddress_3,
            userAgent = "testUserAgent"),
        EchoClick(
            id = UUID.randomUUID.toString,
            updatedOn = on,
            createdOn = on,
            echoId = echoId_1,
            facebookPostId = null,
            twitterStatusId = twitterStatusId_1,
            echoedUserId = echoedUserId,
            browserId = UUID.randomUUID().toString,
            referrerUrl = "http://twitter.com",
            ipAddress = ipAddress_3,
            userAgent = "testUserAgent")
    )

    val echoClicks_2 = List(
        EchoClick(
            id = UUID.randomUUID.toString,
            updatedOn = on,
            createdOn = on,
            echoId = echoId_2,
            facebookPostId = facebookPostId_2,
            twitterStatusId = null,
            echoedUserId = echoedUserId,
            browserId = UUID.randomUUID().toString,
            referrerUrl = "http://facebook.com",
            ipAddress = ipAddress_4,
            userAgent = "testUserAgent"),
        EchoClick(
            id = UUID.randomUUID.toString,
            updatedOn = on,
            createdOn = on,
            echoId = echoId_2,
            facebookPostId = null,
            twitterStatusId = twitterStatusId_2,
            echoedUserId = echoedUserId,
            browserId = UUID.randomUUID().toString,
            referrerUrl = "http://twitter.com",
            ipAddress = ipAddress_4,
            userAgent = "testUserAgent"))

    val echoFuture =  Echo(
            id = echoId_1,
            updatedOn = on,
            createdOn = on,
            retailerId = retailerId,
            echoedUserId = echoedUserId,
            facebookPostId = facebookPostId_1,
            twitterStatusId = twitterStatusId_1,
            echoPossibilityId = echoPossibilities(0).id,
            retailerSettingsId = retailerSettingsFuture.id,
            echoMetricsId = null,
            echoClickId = null,
            step = "test",
            browserId = null,
            ipAddress = null,
            userAgent = null,
            referrerUrl = null,
            view = null,
            order = Order(echoId_1, on, on, customerId_1, on, orderId_1),
            product = Product(echoId_1, on, on, productId_1, price_1, landingPageUrl, productName_1, category_1, brand_1, description_1),
            image = new Image(echoImageUrl_1))



    val noImage = new Image(imagesUrl + "/%s" format "does-not-exist.jpg")
    val smallImage = new Image(imagesUrl + "/%s" format "bg-blackPx.png")

    val images = List(
        new Image(echoImageUrl_1),
        new Image(echoImageUrl_2))


    val geoLocations = List(
        GeoLocation(
            id = UUID.randomUUID().toString,
            updatedOn = on,
            createdOn = on,
            ipAddress = ipAddress_1,
            countryCode =  "US",
            countryName = "United States",
            regionCode = "New York",
            regionName = "New York",
            city = "New York",
            postcode = "10017",
            latitude = "40.7488",
            longitude = "-73.9846",
            isp = "isp",
            organization = "organization",
            updateStatus = "updateStatus"),
        GeoLocation(
            id = UUID.randomUUID().toString,
            updatedOn = on,
            createdOn = on,
            ipAddress = ipAddress_2,
            countryCode =  "US",
            countryName = "United States",
            regionCode = "New York",
            regionName = "New York",
            city = "New York",
            postcode = "10017",
            latitude = "40.7488",
            longitude = "-73.9846",
            isp = "isp",
            organization = "organization",
            updateStatus = "updateStatus"),
        GeoLocation(
            id = UUID.randomUUID().toString,
            updatedOn = on,
            createdOn = on,
            ipAddress = ipAddress_3,
            countryCode =  "US",
            countryName = "United States",
            regionCode = "New York",
            regionName = "New York",
            city = "New York",
            postcode = "10017",
            latitude = "40.7488",
            longitude = "-73.9846",
            isp = "isp",
            organization = "organization",
            updateStatus = "updateStatus"),
        GeoLocation(
            id = UUID.randomUUID().toString,
            updatedOn = on,
            createdOn = on,
            ipAddress = ipAddress_4,
            countryCode =  "US",
            countryName = "United States",
            regionCode = "New York",
            regionName = "New York",
            city = "New York",
            postcode = "10017",
            latitude = "40.7488",
            longitude = "-73.9846",
            isp = "isp",
            organization = "organization",
            updateStatus = "updateStatus"))



    private val _echoes = List(
        Echo(
            id = echoId_1,
            updatedOn = on,
            createdOn = on,
            retailerId = retailerId,
            echoedUserId = echoedUserId,
            facebookPostId = facebookPostId_1,
            twitterStatusId = twitterStatusId_1,
            echoPossibilityId = echoPossibilities(0).id,
            retailerSettingsId = retailerSettings.id,
            echoMetricsId = null,
            echoClickId = null,
            step = "test",
            browserId = null,
            ipAddress = ipAddress_1,
            userAgent = null,
            referrerUrl = null,
            view = null,
            order = Order(echoId_1, on, on, customerId_1, on, orderId_1),
            product = Product(echoId_1, on, on, productId_1, price_1, landingPageUrl, productName_1, category_1, brand_1, description_1),
            image = images(0)),
        Echo(
            id = echoId_2,
            updatedOn = on,
            createdOn = on,
            retailerId = retailerId,
            echoedUserId = echoedUserId,
            facebookPostId = facebookPostId_2,
            twitterStatusId = twitterStatusId_2,
            echoPossibilityId = echoPossibilities(1).id,
            retailerSettingsId = retailerSettings.id,
            echoMetricsId = null,
            echoClickId = null,
            step = "test",
            browserId = null,
            ipAddress = ipAddress_2,
            userAgent = null,
            referrerUrl = null,
            view = null,
            order = Order(echoId_2, on, on, customerId_2, on, orderId_2),
            product = Product(echoId_2, on, on, productId_2, price_2, landingPageUrl, productName_2, category_2, brand_2, description_2),
            image = images(1)))

    val echoMetrics = _echoes.map(new EchoMetrics(_, retailerSettings))
    val echoes = echoMetrics.zip(_echoes).map { tuple =>
        val (em, e) = tuple
        e.copy(echoMetricsId = em.id)
    }
}
