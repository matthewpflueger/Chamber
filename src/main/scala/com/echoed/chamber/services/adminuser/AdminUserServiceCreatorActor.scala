package com.echoed.chamber.services.adminuser

import reflect.BeanProperty
import org.slf4j.LoggerFactory
import scalaz._
import Scalaz._

import akka.actor.{Channel, Actor}

import com.echoed.chamber.dao.AdminUserDao
import com.echoed.chamber.dao.views.AdminViewDao
import com.echoed.chamber.domain.AdminUser
import com.echoed.chamber.dao.partner.PartnerSettingsDao

class AdminUserServiceCreatorActor extends Actor{

    private val logger = LoggerFactory.getLogger(classOf[AdminUserServiceCreatorActor])

    @BeanProperty var adminUserDao: AdminUserDao = _
    @BeanProperty var adminViewDao: AdminViewDao = _
    @BeanProperty var partnerSettingsDao: PartnerSettingsDao = _

    def receive = {
        case msg @ CreateAdminUserService(email) =>
            logger.debug("Loading AdminUser for {}", email)
            Option(adminUserDao.findByEmail(email)).cata(
                au => self.channel ! CreateAdminUserServiceResponse(msg, Right(new AdminUserServiceActorClient(
                    Actor.actorOf(new AdminUserServiceActor(au, adminUserDao, adminViewDao, partnerSettingsDao)).start))),
                self.channel ! CreateAdminUserServiceResponse(msg, Left(new AdminUserException(
                    "No user with email %s" format email))))
        case msg @CreateAdminUser(email,name,password) =>
            logger.debug("Creating Admin User: {}:{}",name,email)
            var adminUser = new AdminUser(name,email)
            adminUser = adminUser.createPassword(password)
            logger.debug("AdminUser: {} ", adminUser)
            adminUserDao.insert(adminUser)
            self.channel ! CreateAdminUserResponse(msg,Right(adminUserDao.findByEmail(email)))
    }

}
