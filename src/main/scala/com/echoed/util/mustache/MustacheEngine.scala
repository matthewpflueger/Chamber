package com.echoed.util.mustache

import com.github.mustachejava.{MustacheException, DefaultMustacheFactory}
import scala.reflect.BeanProperty
import org.springframework.context.ResourceLoaderAware
import org.springframework.core.io.ResourceLoader
import java.io.{StringWriter, InputStreamReader, Reader}
import java.util.{Map => JMap}
import com.github.mustachejava.reflect.ReflectionObjectHandler
import java.lang.reflect.{Method, Field}
import org.slf4j.LoggerFactory

class MustacheEngine(
            @BeanProperty var prefix: String = "",
            @BeanProperty var suffix: String = "",
            @BeanProperty var cache: Boolean = true,
            var resourceLoader: ResourceLoader = null)
        extends DefaultMustacheFactory
        with ResourceLoaderAware {

    private final val logger = LoggerFactory.getLogger(classOf[MustacheEngine])

    def this() = this("", "", true, null)

    setObjectHandler(new ReflectionObjectHandler {
        /* allow access to private fields and methods */
        override def checkField(member: Field) {}
        override def checkMethod(member: Method) {}
    })

    override def getReader(resourceName: String): Reader = {
        var rn = resourceName
        rn = if (!rn.startsWith(prefix)) prefix + rn else rn
        rn = if (!rn.endsWith(suffix)) rn + suffix else rn

        val resource = resourceLoader.getResource(rn)
        if (!resource.exists) throw new MustacheException("No template exists named: " + rn)
        logger.debug("Found template {}", rn)
        new InputStreamReader(resource.getInputStream)
    }

    override def compile(templateName: String) = {
        if (cache) super.compile(templateName)
        else super.compile(getReader(templateName), templateName)
    }

    def execute(templateName: String, model: JMap[String, AnyRef]) = {
        val writer = new StringWriter()
        compile(templateName).execute(writer, model)
        writer.toString
    }

    def setResourceLoader(loader: ResourceLoader) {
        resourceLoader = loader
    }
}
