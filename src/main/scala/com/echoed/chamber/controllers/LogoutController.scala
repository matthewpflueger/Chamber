package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import reflect.BeanProperty
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.echoeduser.EchoedUserServiceLocator
import com.echoed.chamber.services.partneruser.PartnerUserServiceLocator
import org.apache.ibatis.session.SqlSessionFactory
import scala.collection.JavaConversions._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.echoed.chamber.services.adminuser.AdminUserServiceLocator


@Controller
@RequestMapping(Array("/logout"))
class LogoutController {

    private val logger = LoggerFactory.getLogger(classOf[LogoutController])

    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var partnerUserServiceLocator: PartnerUserServiceLocator = _
    @BeanProperty var adminUserServiceLocator: AdminUserServiceLocator = _
    @BeanProperty var postLogoutView: String = _

    @BeanProperty var cookieManager: CookieManager = _

    @BeanProperty var sqlSessionFactory: SqlSessionFactory = _

    @RequestMapping(method = Array(RequestMethod.GET))
    def logout(
            @RequestParam(value = "redirect", required = false) redirect: String,
            @RequestParam(value = "flush", required = false) flush: String,
            httpServletRequest: HttpServletRequest,
            httpServletResponse: HttpServletResponse) = {

        cookieManager.findEchoedUserCookie(httpServletRequest).foreach(echoedUserServiceLocator.logout(_))
        cookieManager.findPartnerUserCookie(httpServletRequest).foreach(partnerUserServiceLocator.logout(_))
        cookieManager.findAdminUserCookie(httpServletRequest).foreach(adminUserServiceLocator.logout(_))

        cookieManager.addEchoedUserCookie(httpServletResponse, request = httpServletRequest)
        cookieManager.addPartnerUserCookie(httpServletResponse, request = httpServletRequest)
        cookieManager.addAdminUserCookie(httpServletResponse, request = httpServletRequest)

        //This is disgusting and will be removed asap!!!
        if (flush == "2390uvqq03rJN_asdfoasdifu190" && Option(sqlSessionFactory) != None) {
            logger.error("Manually flushing caches - you should never, ever see this in production!!!!!!!")
            val caches = sqlSessionFactory.getConfiguration.getCaches
            caches.foreach { cache =>
                logger.error("Flushing cache %s" format cache.getId)
                cache.getReadWriteLock.readLock()
                val lock = cache.getReadWriteLock().writeLock()
                lock.lock()
                try {
                    cache.clear
                } finally {
                    lock.unlock
                }
            }
        }


        new ModelAndView("%s/%s" format (postLogoutView, Option(redirect).getOrElse("")))
    }

}
