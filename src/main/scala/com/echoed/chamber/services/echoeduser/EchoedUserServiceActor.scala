package com.echoed.chamber.services.echoeduser

import akka.actor.Actor
import com.echoed.chamber.domain.EchoedUser
import com.echoed.chamber.dao.EchoedUserDao
import com.echoed.chamber.services.facebook.FacebookService
import com.echoed.chamber.services.twitter.TwitterService
import com.echoed.chamber.domain.TwitterFollower


class EchoedUserServiceActor(
        echoedUser: EchoedUser,
        echoedUserDao: EchoedUserDao,
        var facebookService: FacebookService,
        var twitterService:TwitterService) extends Actor {

    def this(echoedUser: EchoedUser, echoedUserDao: EchoedUserDao) = this(echoedUser, echoedUserDao, null, null)
    def this(echoedUser: EchoedUser, echoedUserDao: EchoedUserDao, facebookService:FacebookService) = this(echoedUser,echoedUserDao,facebookService,null)
    def this(echoedUser: EchoedUser, echoedUserDao: EchoedUserDao, twitterService:TwitterService) = this(echoedUser,echoedUserDao,null,twitterService)


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
          self.channel ! twitterService.updateStatus(status)
          //TODO add ONRESULT etc...
        }

        case("getTwitterFollowers") =>{
          self.channel ! twitterService.getFollowers().get.asInstanceOf[Array[TwitterFollower]]
        }
    }
}
