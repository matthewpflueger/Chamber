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
                val payload = new ScalaObjectMapper().readTree(encrypter.decrypt(puc))
                PartnerUserClientCredentials(
                        payload.get("id").asText,
                        Option(payload.get("name")).map(_.asText),
                        Option(payload.get("email")).map(_.asText),
                        Option(payload.get("partnerId")).map(_.asText),
                        Option(payload.get("partnerName")).map(_.asText))
            }
}
