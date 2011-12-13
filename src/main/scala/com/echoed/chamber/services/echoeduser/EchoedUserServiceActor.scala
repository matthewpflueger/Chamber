package com.echoed.chamber.services.echoeduser

import akka.actor.Actor
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import com.echoed.chamber.dao.views.ClosetDao
import org.slf4j.LoggerFactory
import scalaz._
import Scalaz._
import com.echoed.chamber.domain._
import akka.dispatch.Future
import com.echoed.chamber.domain.EchoedFriend
import com.echoed.chamber.dao.{EchoedFriendDao, EchoedUserDao}
import scala.collection.JavaConversions._
import scala.collection.mutable.{Map => MMap, ListBuffer => MList}


class EchoedUserServiceActor(
        echoedUser: EchoedUser,
        echoedUserDao: EchoedUserDao,
        closetDao: ClosetDao,
        echoedFriendDao: EchoedFriendDao,
        var facebookService: FacebookService,
        var twitterService: TwitterService) extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[EchoedUserServiceActor])

    def this(echoedUser: EchoedUser, echoedUserDao: EchoedUserDao, closetDao: ClosetDao, echoedFriendDao: EchoedFriendDao) =
            this(echoedUser, echoedUserDao, closetDao, echoedFriendDao, null, null)
    def this(echoedUser: EchoedUser, echoedUserDao: EchoedUserDao, closetDao: ClosetDao, echoedFriendDao: EchoedFriendDao, facebookService:FacebookService) =
            this(echoedUser,echoedUserDao, closetDao, echoedFriendDao, facebookService, null)
    def this(echoedUser: EchoedUser, echoedUserDao: EchoedUserDao, closetDao: ClosetDao, echoedFriendDao: EchoedFriendDao, twitterService:TwitterService) =
            this(echoedUser,echoedUserDao, closetDao, echoedFriendDao, null, twitterService)


    override def preStart() {
        //kick off a refresh of our friends...
        (self ? '_fetchEchoedFriends).mapTo[List[EchoedFriend]].map { efs =>
            logger.debug("Successfully refreshed {} EchoedFriends for EchoedUser {}", efs.length, echoedUser.id)
        }
    }

    def receive = {
        case "echoedUser" => self.channel ! echoedUser

        case ("assignTwitterService",twitterService:TwitterService) => {
            this.twitterService = twitterService
            self.channel ! this.twitterService
        }
        case ("assignFacebookService",facebookService:FacebookService) =>{
            this.facebookService = facebookService
            self.channel ! this.facebookService
        }

        case("getTwitterFollowers") =>{
            self.channel ! twitterService.getFollowers().get.asInstanceOf[Array[TwitterFollower]]
        }

        case ("echoToTwitter", echo:Echo,  message:String) =>
            val channel = self.channel
            twitterService.echo(echo,message).map { channel ! _ }

        case ("echoToFacebook", echo: Echo, message: String) =>
            val channel = self.channel
            facebookService.echo(echo, message).map[FacebookPost] { fp: FacebookPost =>
                channel ! fp
                fp
            }

        case "closet" =>
            self.channel ! closetDao.findByEchoedUserId(echoedUser.id)

        case 'friends =>
            val channel = self.channel
            Future {
                logger.debug("Loading EchoedFriends from database for EchoedUser {}", echoedUser.id)
                val echoedFriends = asScalaBuffer(echoedFriendDao.findByEchoedUserId(echoedUser.id)).toList
                channel ! echoedFriends
                logger.debug("Found {} EchoedFriends in database for EchoedUser", echoedFriends.length, echoedUser.id)
            }

        case '_fetchEchoedFriends =>
            val futureFacebookEchoedUsers: Future[List[EchoedUser]] = Option(facebookService).cata(
                    fs => fs.fetchFacebookFriends().map { ffs: List[FacebookFriend] =>
                        logger.debug("Fetched {} FacebookFriends for EchoedUser {}", ffs.length, echoedUser.id)
                        val eus = ffs
                            .map(ff => Option(echoedUserDao.findByFacebookId(ff.facebookId)))
                            .filter(_.isDefined)
                            .map(_.get)
                        logger.debug("Found {} friends via Facebook for EchoedUser {}", eus.length, echoedUser.id)
                        eus
                    },
                    Future {
                        logger.debug("No FacebookService for EchoedUser {}", echoedUser.id)
                        List[EchoedUser]()
                    })

            val futureTwitterEchoedUsers: Future[List[EchoedUser]] = Option(twitterService).cata(
                    ts => ts.getFollowers().map { tfs: List[TwitterFollower] =>
                        logger.debug("Fetched {} TwitterFollowers for EchoedUser {}", tfs.length, echoedUser.id)
                        val eus = tfs
                            .map(tf => Option(echoedUserDao.findByTwitterId(tf.twitterId)))
                            .filter(_.isDefined)
                            .map(_.get)
                        logger.debug("Found {} friends via Twitter for EchoedUser {}", eus.length, echoedUser.id)
                        eus
                    },
                    Future {
                        logger.debug("No TwitterService for EchoedUser {}", echoedUser.id)
                        List[EchoedUser]()
                    })

            val channel = self.channel
            for {
                facebookEchoedUsers <- futureFacebookEchoedUsers
                twitterEchoedUsers <- futureTwitterEchoedUsers
            } yield {
                logger.debug("Creating EchoedFriends for EchoedUser {}", echoedUser.id)
                val map = MMap.empty[String, (EchoedFriend, EchoedFriend)]


                def createEchoedFriends(fromEchoedUser: EchoedUser, toEchoedUser: EchoedUser) =
                    (new EchoedFriend(fromEchoedUser, toEchoedUser), new EchoedFriend(toEchoedUser, fromEchoedUser))

                facebookEchoedUsers.foreach(eu => map(eu.id) = createEchoedFriends(echoedUser, eu))

                twitterEchoedUsers.foreach(eu => if (!map.contains(eu.id)) map(eu.id) = createEchoedFriends(echoedUser, eu))

                var efs = MList[EchoedFriend]()
                map.values.foreach { tuple =>
                    val (mine, theirs) = tuple
                    efs += mine
                    echoedFriendDao.insertOrUpdate(mine)
                    echoedFriendDao.insertOrUpdate(theirs)
                }

                channel ! efs
                logger.debug("Saved {} EchoedFriends for {}", efs.length, echoedUser)
            }

    }
}
