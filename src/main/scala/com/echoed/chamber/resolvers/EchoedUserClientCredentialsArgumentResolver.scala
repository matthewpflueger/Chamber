package com.echoed.chamber.resolvers

import org.springframework.core.MethodParameter
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import javax.servlet.http.HttpServletRequest
import com.echoed.util.ScalaObjectMapper

class EchoedUserClientCredentialsArgumentResolver extends ClientCredentialsArgumentResolver {

    def supportsParameter(parameter: MethodParameter) =
            classOf[EchoedUserClientCredentials].isAssignableFrom(parameter.getParameterType)

    protected val redirectPath = Option("/")

    protected val requestAttribute = "echoedUserClientCredentials"

    protected def resolveCredentials(request: HttpServletRequest) =
            cookieManager.findEchoedUserCookie(request).map { euc =>
                val payload = new ScalaObjectMapper()
                        .readValue(encrypter.decrypt(euc), classOf[Map[String, String]])
                        .filter(kv => kv._2 != null)
                EchoedUserClientCredentials(
                        payload.get("id").get,
                        payload.get("name"),
                        payload.get("email"),
                        payload.get("screenName"),
                        payload.get("facebookId"),
                        payload.get("twitterId"),
                        payload.get("password"))
            }

}
