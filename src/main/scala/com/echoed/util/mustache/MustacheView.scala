package com.echoed.util.mustache

import org.springframework.web.servlet.view.AbstractTemplateView
import scala.reflect.BeanProperty
import com.samskivert.mustache.Template
import java.util.{Map => JMap}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

class MustacheView extends AbstractTemplateView {

    @BeanProperty var template: Template = null


    def renderMergedTemplateToString(model: JMap[String, Object]) = template.execute(model)


    protected override def renderMergedTemplateModel(
            model: JMap[String, Object],
            request: HttpServletRequest,
            response: HttpServletResponse) {

        response.setContentType(getContentType())
        val writer = response.getWriter()
        try {
            template.execute(model, writer);
        } finally {
            writer.flush();
        }
    }

    override def getContentType() = {
        if (getBeanName.endsWith(".js")) "application/x-javascript"
        else if (getBeanName.endsWith(".json")) "application/json"
        else super.getContentType
    }

}
