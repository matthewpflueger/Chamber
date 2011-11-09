package com.echoed.chamber.services

import akka.actor.Actor
import reflect.BeanProperty
import com.echoed.chamber.dao.{RetailerDao, EchoPossibilityDao}
import akka.dispatch.Future
import com.echoed.chamber.domain.{Retailer, EchoPossibility}


class EchoServiceActor extends Actor {

    @BeanProperty var echoPossibilityDao: EchoPossibilityDao = null
    @BeanProperty var retailerDao: RetailerDao = null

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
    }
}