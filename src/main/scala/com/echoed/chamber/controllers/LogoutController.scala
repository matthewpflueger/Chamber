package com.echoed.chamber.controllers

import org.springframework.stereotype.Controller
import javax.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation._
import reflect.BeanProperty
import com.echoed.util.CookieManager
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.ModelAndView
import com.echoed.chamber.services.echoeduser.EchoedUserServiceLocator
import com.echoed.chamber.services.partneruser.PartnerUserServiceLocator
import org.apache.ibatis.session.SqlSessionFactory
import scala.collection.JavaConversions._


@Controller
@RequestMapping(Array("/logout"))
class LogoutController {

    private val logger = LoggerFactory.getLogger(classOf[LogoutController])

    @BeanProperty var echoedUserServiceLocator: EchoedUserServiceLocator = _
    @BeanProperty var partnerUserServiceLocator: PartnerUserServiceLocator = _

    @BeanProperty var cookieManager: CookieManager = _

    @BeanProperty var sqlSessionFactory: SqlSessionFactory = _

    @RequestMapping(method = Array(RequestMethod.GET))
    def logout(
            @RequestParam(value = "redirect", required = false) redirect: String,
            @RequestParam(value = "flush", required = false) flush: String,
            @CookieValue(value = "echoedUserId", required = false) echoedUserId: String,
            @CookieValue(value = "partnerUser", required = false) partnerUser: String,
            httpServletResponse: HttpServletResponse) = {


        logger.debug("Removing cookies: echoedUserId {} and partnerUser {}", echoedUserId, partnerUser);
        cookieManager.deleteCookie(httpServletResponse, "echoedUserId");
        cookieManager.deleteCookie(httpServletResponse, "partnerUser");
        Option(echoedUserId).foreach(echoedUserServiceLocator.logout(_))
        Option(partnerUser).foreach(partnerUserServiceLocator.logout(_))

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


        if (redirect != null)
            new ModelAndView("redirect:http://v1-api.echoed.com/" + redirect);
        else
            new ModelAndView("redirect:http://www.echoed.com/")

    }

}
