package com.echoed.util.mustache

import com.samskivert.mustache.Mustache
import org.springframework.beans.factory.InitializingBean
import scala.reflect.BeanProperty
import org.springframework.context.ResourceLoaderAware
import org.springframework.core.io.ResourceLoader

class MustacheEngine extends InitializingBean with ResourceLoaderAware {

    @BeanProperty var templateLoader: MustacheTemplateLoader = null
    @BeanProperty var standardsMode: Boolean = false
    @BeanProperty var escapeHTML: Boolean = true
    @BeanProperty var defaultValue: String = null
    @BeanProperty var prefix: String = ""
    @BeanProperty var suffix: String = ""

    private var compiler: Mustache.Compiler = null
    private var resourceLoader: ResourceLoader = null


    def compile(templateName: String) = compiler.compile(templateLoader.getTemplate(templateName))

    def compileTemplateString(templateString: String) = compiler.compile(templateString)

    def afterPropertiesSet() {
        templateLoader = Option(templateLoader).getOrElse(new MustacheTemplateLoader(prefix, suffix, resourceLoader))
        compiler = Mustache.compiler
                .escapeHTML(escapeHTML)
                .standardsMode(standardsMode)
                .withLoader(templateLoader)
        compiler = if (defaultValue != null && defaultValue != "null") compiler.defaultValue(defaultValue) else compiler
    }

    def setResourceLoader(loader: ResourceLoader) {
        resourceLoader = loader
    }
}
