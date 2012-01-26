package com.echoed.util.mustache

import scala.reflect.BeanProperty
import com.samskivert.mustache.Mustache.TemplateLoader
import org.springframework.context.ResourceLoaderAware
import org.springframework.core.io.ResourceLoader
import java.io.{InputStreamReader, Reader}

class MustacheTemplateLoader(
        @BeanProperty var prefix: String = "",
        @BeanProperty var suffix: String = "",
        var resourceLoader: ResourceLoader = null) extends TemplateLoader with ResourceLoaderAware {

    def this() = this("", "", null)

    def getTemplate(fileName: String): Reader = {
        var fn = fileName
        fn = if (!fn.startsWith(prefix)) prefix + fn else fn
        fn = if (!fn.endsWith(suffix)) fn + suffix else fn

        new InputStreamReader(resourceLoader.getResource(fn).getInputStream)
    }

    def setResourceLoader(loader: ResourceLoader) {
        resourceLoader = loader
    }
}
