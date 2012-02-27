package com.echoed.util.mustache

import org.springframework.web.servlet.ViewResolver
import org.springframework.web.servlet.view.{AbstractTemplateViewResolver, AbstractUrlBasedView}
import scala.reflect.BeanProperty

class MustacheViewResolver extends AbstractTemplateViewResolver with ViewResolver {

    @BeanProperty var mustacheEngine: MustacheEngine = _

    setViewClass(classOf[MustacheView])

    protected override def requiredViewClass: Class[_] = {
        return classOf[MustacheView]
    }

    protected override def buildView(viewName: String): AbstractUrlBasedView = {
        val view = super.buildView(viewName).asInstanceOf[MustacheView]
        val template = mustacheEngine.compile(view.getUrl)
        view.setTemplate(template)
        view
    }

}
