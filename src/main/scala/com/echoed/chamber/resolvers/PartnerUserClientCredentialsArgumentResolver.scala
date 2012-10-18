package com.echoed.chamber.resolvers

import org.springframework.core.MethodParameter
import javax.servlet.http.HttpServletRequest
import com.echoed.chamber.services.partneruser.PartnerUserClientCredentials
import com.echoed.util.ScalaObjectMapper

class PartnerUserClientCredentialsArgumentResolver extends ClientCredentialsArgumentResolver {

    def supportsParameter(parameter: MethodParameter) =
        classOf[PartnerUserClientCredentials].isAssignableFrom(parameter.getParameterType)

    protected val redirectPath = Option("/partner/login")

    protected val requestAttribute = "partnerUserClientCredentials"

    protected def resolveCredentials(request: HttpServletRequest) =
            cookieManager.findPartnerUserCookie(request).map { puc =>
                val payload = new ScalaObjectMapper()
                        .readValue(encrypter.decrypt(puc), classOf[Map[String, String]])
                        .filter(kv => kv._2 != null)
                PartnerUserClientCredentials(
                        payload.get("id").get,
                        payload.get("name"),
                        payload.get("email"),
                        payload.get("partnerId"),
                        payload.get("partnerName"))
            }
}
