package com.echoed.chamber.resolvers

import org.springframework.core.MethodParameter
import com.echoed.chamber.services.echoeduser.EchoedUserClientCredentials
import org.springframework.beans.factory.annotation.Autowired
import javax.servlet.http.HttpServletRequest
import com.echoed.util.{ScalaObjectMapper, Encrypter}

class EchoedUserClientCredentialsArgumentResolver extends ClientCredentialsArgumentResolver {

    @Autowired var encrypter: Encrypter = _

    def supportsParameter(parameter: MethodParameter) =
            classOf[EchoedUserClientCredentials].isAssignableFrom(parameter.getParameterType)

    protected val redirectPath = Option("/")

    protected val requestAttribute = "echoedUserClientCredentials"

    protected def resolveCredentials(request: HttpServletRequest) =
            cookieManager.findEchoedUserCookie(request).map { euc =>
                val payload = new ScalaObjectMapper().readTree(encrypter.decrypt(euc))
                EchoedUserClientCredentials(
                        payload.get("id").asText,
                        Option(payload.get("name")).map(_.asText),
                        Option(payload.get("email")).map(_.asText),
                        Option(payload.get("screenName")).map(_.asText),
                        Option(payload.get("facebookId")).map(_.asText),
                        Option(payload.get("twitterId")).map(_.asText))
            }

}
