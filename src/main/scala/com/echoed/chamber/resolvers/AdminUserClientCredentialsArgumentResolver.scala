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
import com.echoed.chamber.services.adminuser.AdminUserClientCredentials

class AdminUserClientCredentialsArgumentResolver extends HandlerMethodArgumentResolver {
    @Autowired var cookieManager: CookieManager = _

    def supportsParameter(parameter: MethodParameter) =
        classOf[AdminUserClientCredentials].isAssignableFrom(parameter.getParameterType)


    def resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory) = {
        cookieManager.findAdminUserCookie(webRequest.getNativeRequest(classOf[HttpServletRequest])).map {
            auc => new AdminUserClientCredentials with EchoedClientCredentials { val id = auc }
        }.getOrElse {
            if (parameter.hasParameterAnnotation(classOf[Nullable])) null
            else throw new IllegalAccessException("No credentials")
        }
    }
}