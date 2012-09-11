package com.echoed.chamber.services

import scala.reflect.BeanProperty
import java.util.Properties
import java.util.{Map => JMap, HashMap => JHashMap}
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory
import com.echoed.chamber.interceptors.GlobalsInterceptor
import org.springframework.context.MessageSource
import java.util.Locale
import com.github.mustachejava.TemplateFunction


class GlobalsManager {

    val logger = LoggerFactory.getLogger(classOf[GlobalsInterceptor])

    @BeanProperty var urlsProperties: Properties = new Properties()
    @BeanProperty var gitProperties: Properties = new Properties()

    @BeanProperty var httpUrlsAttributeName = "http"
    @BeanProperty var httpsUrlsAttributeName = "https"
    @BeanProperty var urlsAttributeName = "urls"
    @BeanProperty var versionAttributeName = "version"
    @BeanProperty var facebookClientIdAttributeName = "facebookClientId"
    @BeanProperty var scriptTagAttributeName = "scriptTag"

    @BeanProperty val httpUrls = new JHashMap[String, String]()
    @BeanProperty val httpsUrls = new JHashMap[String, String]()

    @BeanProperty var envType: String = _

    @BeanProperty var version = ""
    @BeanProperty var facebookClientId = ""

    @BeanProperty var i18nKey = "i18n"
    @BeanProperty var messageSource: MessageSource = _

    @BeanProperty var scriptTagTemplate: String = _

    private var scriptTagFunction: TemplateFunction = _


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

        if (version == null || version.length() < 1) {
            version = gitProperties.getProperty("git.commit.id.abbrev", "")
        }

        val versionInfo = new StringBuilder()
        gitProperties.stringPropertyNames().foreach { key =>
            versionInfo
                    .append("    ")
                    .append(key)
                    .append(": ")
                    .append(gitProperties.getProperty(key))
                    .append("\n")
        }

        scriptTagFunction = new TemplateFunction {
            def apply(input: String) = scriptTagTemplate format input
        }

        logger.error("Booting Chamber:\n" + versionInfo)
    }

    def addGlobals(model: JMap[String, AnyRef]) {
        addGlobals(model, httpUrls)
    }

    def addGlobals(model: JMap[String, AnyRef], urls: JMap[String, String]) {
        addGlobals(model, urls, null)
    }

    def addGlobals(model: JMap[String, AnyRef], urls: JMap[String, String], locale: Locale) {
        model.put(envType, envType)
        model.put(versionAttributeName, version)
        model.put(facebookClientIdAttributeName, facebookClientId)

        model.put(httpUrlsAttributeName, httpUrls)
        model.put(httpsUrlsAttributeName, httpsUrls)

        model.put(urlsAttributeName, urls)
        model.put(scriptTagAttributeName, scriptTagFunction)

        model.put(i18nKey, new Function[String, String]() {
            def apply(input: String) = messageSource.getMessage(input, null, locale)
        })
    }

}
