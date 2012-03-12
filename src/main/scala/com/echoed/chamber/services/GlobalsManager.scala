package com.echoed.chamber.services

import scala.reflect.BeanProperty
import java.util.Properties
import java.util.{Map => JMap, HashMap => JHashMap}
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory
import com.echoed.chamber.interceptors.GlobalsInterceptor


class GlobalsManager {

    val logger = LoggerFactory.getLogger(classOf[GlobalsInterceptor])

    @BeanProperty var urlsProperties: Properties = new Properties()
    @BeanProperty var gitProperties: Properties = new Properties()

    @BeanProperty var urlsAttributeName = "urls"
    @BeanProperty var versionAttributeName = "version"
    @BeanProperty var facebookClientIdAttributeName = "facebookClientId"

    @BeanProperty val httpUrls = new JHashMap[String, String]()
    @BeanProperty val httpsUrls = new JHashMap[String, String]()
    @BeanProperty var version = ""
    @BeanProperty var facebookClientId = ""


    def init() {
        urlsProperties.filterKeys(_.toString.startsWith("http.urls.")).foreach {
            tuple =>
                val (key, value) = tuple
                httpUrls.put(key.replace("http.urls.", ""), value)
        }
        urlsProperties.filterKeys(_.toString.startsWith("https.urls.")).foreach {
            tuple =>
                val (key, value) = tuple
                httpsUrls.put(key.replace("https.urls.", ""), value)
        }
        version = gitProperties.getProperty("git.commit.id.abbrev", "")
        val versionInfo = new StringBuilder()
        gitProperties.stringPropertyNames().foreach { key =>
            versionInfo
                    .append("    ")
                    .append(key)
                    .append(": ")
                    .append(gitProperties.getProperty(key))
                    .append("\n")
        }
        logger.error("Booting Chamber:\n" + versionInfo)

    }

    def addGlobals(model: JMap[String, AnyRef]) {
        addGlobals(model, httpUrls)
    }

    def addGlobals(model: JMap[String, AnyRef], urls: JMap[String, String]) {
        model.put(versionAttributeName, version)
        model.put(facebookClientIdAttributeName, facebookClientId)

        if (model.get(urlsAttributeName) == null) {
            model.put(urlsAttributeName, urls)
        } else if (model.get(urlsAttributeName).isInstanceOf[JMap[String, String]]) {
            urls.putAll(model.get(urlsAttributeName).asInstanceOf[JMap[String, String]])
            model.put(urlsAttributeName, urls)
        } else {
            logger.debug("Attribute of name {} already exists: {}", urlsAttributeName, model.get(urlsAttributeName))
        }
    }
}
