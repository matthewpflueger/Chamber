package com.echoed.chamber.interceptors

//import org.apache.log4j.Logger

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.springframework.web.servlet.{ModelAndView, HandlerInterceptor}
import org.slf4j.LoggerFactory
import org.eclipse.jetty.continuation.ContinuationSupport
import scala.reflect.BeanProperty
import java.util.Properties
import scala.collection.JavaConversions._
import java.util.{Map => JMap, HashMap => JHashMap}

class GlobalsInterceptor extends HandlerInterceptor {

    val logger = LoggerFactory.getLogger(classOf[GlobalsInterceptor])

    @BeanProperty var urlsProperties: Properties = new Properties()
    @BeanProperty var gitProperties: Properties = new Properties()

    @BeanProperty var urlsAttributeName = "urls"
    @BeanProperty var versionAttributeName = "version"
    @BeanProperty var facebookClientIdAttributeName = "facebookClientId"
    @BeanProperty var facebookClientId = ""

    private val httpUrls = new JHashMap[AnyRef, AnyRef]()
    private val httpsUrls = new JHashMap[AnyRef, AnyRef]()
    private var version = ""


    def init() {
        urlsProperties.filterKeys(_.toString.startsWith("http.urls.")).foreach { tuple =>
            val (key, value) = tuple
            httpUrls.put(key.replace("http.urls.", ""), value)
        }
        urlsProperties.filterKeys(_.toString.startsWith("https.urls.")).foreach { tuple =>
            val (key, value) = tuple
            httpsUrls.put(key.replace("https.urls.", ""), value)
        }
        version = gitProperties.getProperty("git.commit.id", "")
    }

    def preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object) = {
        true;
    }

    def postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object, modelAndView: ModelAndView) {
        Option(modelAndView).foreach { mv =>
            if (mv.getViewName != null && !mv.getViewName.startsWith("redirect:")) {
                val model = mv.getModel

                if (model.get(versionAttributeName) == null) {
                    model.put(versionAttributeName, version)
                } else {
                    logger.debug("Attribute of name {} already exists: {}", versionAttributeName, model.get(versionAttributeName))
                }

                if (model.get(facebookClientIdAttributeName) == null) {
                    model.put(facebookClientIdAttributeName, facebookClientId)
                } else {
                    logger.debug("Attribute of name {} already exists: {}", facebookClientIdAttributeName, model.get(facebookClientIdAttributeName))
                }

                val urls = new JHashMap[AnyRef, AnyRef](if (request.getProtocol == "https") httpsUrls else httpUrls)

                if (model.get(urlsAttributeName) == null) {
                    model.put(urlsAttributeName, urls)
                } else if (model.get(urlsAttributeName).isInstanceOf[JMap[AnyRef, AnyRef]]) {
                    urls.putAll(model.get(urlsAttributeName).asInstanceOf[JMap[AnyRef, AnyRef]])
                    model.put(urlsAttributeName, urls)
                } else {
                    logger.debug("Attribute of name {} already exists: {}", urlsAttributeName, model.get(urlsAttributeName))
                }
            }
        }
    }

    def afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Object, ex: Exception) {
        //
    }
}
