package com.echoed.chamber.interceptors

import org.springframework.web.servlet.{ModelAndView, HandlerInterceptor}
import org.slf4j.LoggerFactory
import scala.reflect.BeanProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.springframework.web.method.HandlerMethod
import org.springframework.web.method.support.{ModelAndViewContainer, HandlerMethodArgumentResolver}
import scala.collection.JavaConversions._
import com.google.common.collect.{LinkedHashMultimap, ListMultimap, Multimaps, HashMultimap}
import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.bind.support.WebDataBinderFactory
import com.echoed.chamber.resolvers.PreflightArgumentResolver
import scala.collection.mutable.ConcurrentMap
import java.util.concurrent.ConcurrentHashMap


class PreflightArgumentResolverInterceptor extends HandlerInterceptor {

    private final val log = LoggerFactory.getLogger(classOf[PreflightArgumentResolverInterceptor])

    @Autowired @BeanProperty var requestMappingHandler: RequestMappingHandlerAdapter = _

    private var argumentResolvers: List[HandlerMethodArgumentResolver] = _

    private val parameterToArgumentResolver /*: ConcurrentMap[Class[_], PreflightArgumentResolver]*/ =
            new ConcurrentHashMap[Class[_], PreflightArgumentResolver]()

//    private val parameterToArgumentResolver = Multimaps.synchronizedMultimap(
//            LinkedHashMultimap.create[Class[_], PreflightArgumentResolver])


    private val noopArgumentResolver = new PreflightArgumentResolver {
        def preflightResolveArgument(
                parameter: MethodParameter,
                request: HttpServletRequest,
                response: HttpServletResponse) = true
    }

    def init() {
        argumentResolvers = requestMappingHandler
                .getCustomArgumentResolvers
                .toList
                .filter(_.isInstanceOf[PreflightArgumentResolver])
    }


    def preHandle(request: HttpServletRequest, response: HttpServletResponse, h: Object) = {
        val handler = h.asInstanceOf[HandlerMethod]
        handler.getMethodParameters.takeWhile { param =>
            parameterToArgumentResolver.getOrElseUpdate(param.getParameterType, {
                argumentResolvers
                    .filter(_.supportsParameter(param))
                    .headOption
                    .map(_.asInstanceOf[PreflightArgumentResolver])
                    .getOrElse(noopArgumentResolver)
            }).preflightResolveArgument(param, request, response)
        }.length == handler.getMethodParameters.length
    }

    def postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object, modelAndView: ModelAndView) {
        //
    }

    def afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Object, ex: Exception) {
        //
    }
}
