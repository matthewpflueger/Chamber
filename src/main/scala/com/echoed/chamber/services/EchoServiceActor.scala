package com.echoed.chamber.services

import akka.actor.Actor
import reflect.BeanProperty
import akka.dispatch.Future
import com.echoed.chamber.services.echoeduser.EchoedUserServiceLocator
import com.echoed.chamber.domain.{Echo, Retailer, EchoPossibility}
import com.echoed.chamber.dao.{EchoDao, RetailerDao, EchoPossibilityDao}


class EchoServiceActor extends Actor {

    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var echoPossibilityDao: EchoPossibilityDao = _
    @BeanProperty var retailerDao: RetailerDao = _
    @BeanProperty var echoDao: EchoDao = _

    def receive = {
        case ("recordEchoPossibility", echoPossibility: EchoPossibility) => {
            val retailer = Future[Option[Retailer]] { Option(retailerDao.findById(echoPossibility.retailerId)) }
            (Option(echoPossibility.id), retailer.get) match {
                case (_, None) => throw new RuntimeException("Invalid retailerId in EchoPossibility %s " format echoPossibility)
                case (None, _) => throw new RuntimeException("Not enough information to record EchoPossibility %s" format echoPossibility)
                case _ => {
                    echoPossibilityDao.insertOrUpdate(echoPossibility)
                    self.channel ! echoPossibility
                }
            }
        }
        case ("echoPossibility", echoPossibilityId: String) => {
            self.channel ! Option(echoPossibilityDao.findById(echoPossibilityId)).getOrElse(None)
        }
        case ("echo", echoedUserId: String, echoPossibilityId: String) => {
            val futureEchoedUserService = echoedUserServiceLocator.getEchoedUserServiceWithId(echoedUserId)
            val echoPossibility = echoPossibilityDao.findById(echoPossibilityId)

            val echo = new Echo(null, futureEchoedUserService.get.echoedUser.get.id, echoPossibility.id)
            echoDao.insertOrUpdate(echo)

            echoPossibility.echoId = echo.id
            echoPossibility.step = "echoed"

            self.channel ! echo
        }
    }
}
