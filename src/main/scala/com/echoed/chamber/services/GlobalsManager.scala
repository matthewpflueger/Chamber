package com.echoed.chamber.services

import scala.reflect.BeanProperty
import java.util.Properties
import java.util.{HashMap => JMap}
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
    @BeanProperty var i18nAttributeName = "i18n"

    @BeanProperty var httpUrls = new JMap[String, String]()
    @BeanProperty var httpsUrls = new JMap[String, String]()

    @BeanProperty var envType: String = _

    @BeanProperty var version = ""
    @BeanProperty var facebookClientId = ""


    @BeanProperty var messageSource: MessageSource = _

    @BeanProperty var scriptTagTemplate: String = _


    private val scriptTagFunction = new TemplateFunction {
        def apply(input: String) = scriptTagTemplate format input
    }

    private var globalsMap: Map[String, AnyRef] = _


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

        globalsMap = Map(
            envType -> envType,
            versionAttributeName -> version,
            facebookClientIdAttributeName -> facebookClientId,
            httpUrlsAttributeName -> httpUrls,
            httpsUrlsAttributeName -> httpsUrls,
            scriptTagAttributeName -> scriptTagFunction)

        logger.error("Booting Chamber:\n" + versionInfo)
    }


    def globals(urls: JMap[String, String] = httpUrls, locale: Locale = Locale.ENGLISH) =
        globalsMap +
        (urlsAttributeName -> urls) +
        (i18nAttributeName -> new Function[String, String]() {
            def apply(input: String) = messageSource.getMessage(input, null, locale)
        })

}
