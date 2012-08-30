package com.echoed.chamber.resolvers

import org.springframework.web.method.support.{ModelAndViewContainer, HandlerMethodArgumentResolver}
import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.beans.factory.annotation.Autowired
import com.echoed.chamber.controllers.CookieManager
import javax.servlet.http.HttpServletRequest
import javax.annotation.Nullable
import com.echoed.chamber.services.EchoedClientCredentials
import com.echoed.chamber.services.partner.PartnerClientCredentials
import org.springframework.web.servlet.HandlerMapping
import java.util.{Map => JMap}


class PartnerClientCredentialsArgumentResolver extends HandlerMethodArgumentResolver {
    @Autowired var cookieManager: CookieManager = _

    def supportsParameter(parameter: MethodParameter) =
        classOf[PartnerClientCredentials].isAssignableFrom(parameter.getParameterType)


    def resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory) = {

        val request = webRequest.getNativeRequest(classOf[HttpServletRequest])
        val variables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE).asInstanceOf[JMap[String, String]]

        Option(webRequest.getParameter("pid")).orElse(Option(variables.get("pid"))).map { pid =>
            new PartnerClientCredentials with EchoedClientCredentials { val id = pid }
        }.getOrElse {
            if (parameter.hasParameterAnnotation(classOf[Nullable])) null
            else throw new IllegalAccessException("No credentials")
        }
    }
}
