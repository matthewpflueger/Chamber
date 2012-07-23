package com.echoed.chamber.resolvers

import org.springframework.web.method.support.{ModelAndViewContainer, HandlerMethodArgumentResolver}
import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.bind.support.WebDataBinderFactory
import com.echoed.chamber.services.echoeduser.{EchoedUserClientCredentials, EchoedUserIdentifiable}
import org.springframework.beans.factory.annotation.Autowired
import com.echoed.chamber.controllers.CookieManager
import javax.servlet.http.HttpServletRequest
import javax.annotation.Nullable
import com.echoed.util.{ScalaObjectMapper, Encrypter}

class EchoedUserClientCredentialsArgumentResolver extends HandlerMethodArgumentResolver {
    @Autowired var encrypter: Encrypter = _
    @Autowired var cookieManager: CookieManager = _

    def supportsParameter(parameter: MethodParameter) =
            classOf[EchoedUserClientCredentials].isAssignableFrom(parameter.getParameterType)


    def resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory) = {
        cookieManager.findEchoedUserCookie(webRequest.getNativeRequest(classOf[HttpServletRequest])).map { euc =>
            val payload = new ScalaObjectMapper().readTree(encrypter.decrypt(euc))
            EchoedUserClientCredentials(
                    payload.get("id").asText,
                    payload.get("name").asText,
                    Option(payload.get("email")).map(_.asText),
                    Option(payload.get("screenName")).map(_.asText),
                    Option(payload.get("facebookId")).map(_.asText),
                    Option(payload.get("twitterId")).map(_.asText))
        }.getOrElse {
            if (parameter.hasParameterAnnotation(classOf[Nullable])) null
            else throw new IllegalAccessException("No credentials")
        }
    }
}
