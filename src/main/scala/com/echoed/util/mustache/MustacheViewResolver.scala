package com.echoed.util.mustache

import com.samskivert.mustache.Mustache
import org.springframework.beans.factory.InitializingBean
import org.springframework.web.servlet.ViewResolver
import org.springframework.web.servlet.view.{AbstractTemplateViewResolver, AbstractUrlBasedView}
import scala.reflect.BeanProperty
import org.springframework.context.ResourceLoaderAware
import org.springframework.core.io.ResourceLoader

class MustacheViewResolver
        extends AbstractTemplateViewResolver
        with ViewResolver
        with InitializingBean
        with ResourceLoaderAware {

    @BeanProperty var templateLoader: MustacheTemplateLoader = null
    @BeanProperty var standardsMode: Boolean = false
    @BeanProperty var escapeHTML: Boolean = true

    private var compiler: Mustache.Compiler = null
    private var resourceLoader: ResourceLoader = null

    setViewClass(classOf[MustacheView])

    protected override def requiredViewClass: Class[_] = {
        return classOf[MustacheView]
    }

    protected override def buildView(viewName: String): AbstractUrlBasedView = {
        val view = super.buildView(viewName).asInstanceOf[MustacheView]
        val template = compiler.compile(templateLoader.getTemplate(view.getUrl))
        view.setTemplate(template)
        view
    }

    def afterPropertiesSet() {
        templateLoader = Option(templateLoader).getOrElse(new MustacheTemplateLoader(getPrefix(), getSuffix(), resourceLoader))
        compiler = Mustache.compiler
                .escapeHTML(escapeHTML)
                .standardsMode(standardsMode)
                .withLoader(templateLoader)
    }

    def setResourceLoader(loader: ResourceLoader) {
        resourceLoader = loader
    }
}
