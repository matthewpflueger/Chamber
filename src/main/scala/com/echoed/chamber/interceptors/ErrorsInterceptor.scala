package com.echoed.chamber.interceptors


import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.springframework.web.servlet.{ModelAndView, HandlerInterceptor}
import org.slf4j.LoggerFactory
import scala.reflect.BeanProperty
import scala.collection.JavaConversions._
import java.util.{HashSet => JHashSet, HashMap => JHashMap, ArrayList => JArrayList}
import org.springframework.web.servlet.support.RequestContextUtils
import org.springframework.context.MessageSource
import org.springframework.validation.{FieldError, Errors}

class ErrorsInterceptor extends HandlerInterceptor {

    val logger = LoggerFactory.getLogger(classOf[ErrorsInterceptor])

    @BeanProperty var errorsAttributeName = "errors"
    @BeanProperty var messageSource: MessageSource = null


    def preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object) = {
        true;
    }

    def postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object, modelAndView: ModelAndView) {
        if (modelAndView == null || modelAndView.getViewName == null) { // || modelAndView.getViewName.startsWith("redirect:")) {
            return
        }

        val errorsMap = modelAndView.getModel.getOrElseUpdate(
                errorsAttributeName,
                new JHashMap[String, AnyRef]).asInstanceOf[JHashMap[String, AnyRef]]
        val global = errorsMap.getOrElseUpdate(
                "global",
                new JArrayList[String]).asInstanceOf[JArrayList[String]]
        val fieldErrors = errorsMap.getOrElseUpdate(
                "fieldErrors",
                new JArrayList[JHashMap[String, AnyRef]]).asInstanceOf[JArrayList[JHashMap[String, AnyRef]]]

        val objects = new JHashMap[String, JHashMap[String, AnyRef]]

        val locale = RequestContextUtils.getLocale(request)

        modelAndView.getModel.values.collect { case e: Errors => e }.foreach { errors: Errors =>
                errors.getGlobalErrors.foreach { e => global.add(messageSource.getMessage(e, locale)) }

                errors.getFieldErrors.foreach { e =>
                    val message = messageSource.getMessage(e, locale)
                    objects.getOrElseUpdate(e.getObjectName, new JHashMap[String, AnyRef]).put(e.getField, message)

                    val fieldMap = new JHashMap[String, AnyRef]
                    fieldMap.put("object", e.getObjectName)
                    fieldMap.put("field", e.getField)
                    fieldMap.put("message", message)

                    fieldErrors.add(fieldMap)
                }
        }

        errorsMap.putAll(objects)


        //if this came from a redirect we want to add the errors passed on the url to our global errors...
        Option(request.getParameterValues(errorsAttributeName)).foreach(_.foreach(global.add(_)))

        //if we are redirecting lets pass on the global errors...
        if (modelAndView.getViewName.startsWith("redirect:")) {
            modelAndView.addObject(errorsAttributeName, global)
        }

        if (logger.isDebugEnabled() && (global.size() > 0 || fieldErrors.size() > 0)) {
            logger.debug("Found errors {}", modelAndView.getModel.get(errorsAttributeName))
        }
    }

    def afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Object, ex: Exception) {
        //
    }
}
