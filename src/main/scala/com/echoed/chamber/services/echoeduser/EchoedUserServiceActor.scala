package com.echoed.chamber.services.echoeduser

import akka.actor.Actor
import com.echoed.chamber.dao.EchoedUserDao
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import com.echoed.chamber.dao.views.ClosetDao
import com.echoed.chamber.domain.{FacebookPost, TwitterFollower, Echo, EchoedUser}
import org.slf4j.LoggerFactory


class EchoedUserServiceActor(
        echoedUser: EchoedUser,
        echoedUserDao: EchoedUserDao,
        closetDao: ClosetDao,
        var facebookService: FacebookService,
        var twitterService:TwitterService) extends Actor {

    private final val logger = LoggerFactory.getLogger(classOf[EchoedUserServiceActor])

    def this(echoedUser: EchoedUser, echoedUserDao: EchoedUserDao, closetDao: ClosetDao) =
            this(echoedUser, echoedUserDao, closetDao, null, null)
    def this(echoedUser: EchoedUser, echoedUserDao: EchoedUserDao, closetDao: ClosetDao, facebookService:FacebookService) =
            this(echoedUser,echoedUserDao, closetDao, facebookService, null)
    def this(echoedUser: EchoedUser, echoedUserDao: EchoedUserDao, closetDao: ClosetDao, twitterService:TwitterService) =
            this(echoedUser,echoedUserDao, closetDao, null, twitterService)


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

        case ("updateTwitterStatus", status:String) =>{
            //TODO Check to make sure there is an active TwitterService
            //TODO this is just a stop gap for now - need a better to handle no TwitterService...
            if (twitterService != null) {
                val channel = self.channel
                twitterService.updateStatus(status).map { channel ! _ }
                //TODO add ONRESULT etc...
            } else {
                //TODO FIXME!!!!!  Should not be sending a null...
                self.channel ! null
            }
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
    }
}
