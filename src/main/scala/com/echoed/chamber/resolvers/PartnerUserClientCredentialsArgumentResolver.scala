package com.echoed.chamber.resolvers

import org.springframework.core.MethodParameter
import javax.servlet.http.HttpServletRequest
import com.echoed.chamber.services.EchoedClientCredentials
import com.echoed.chamber.services.partneruser.PartnerUserClientCredentials

class PartnerUserClientCredentialsArgumentResolver extends ClientCredentialsArgumentResolver {

    def supportsParameter(parameter: MethodParameter) =
        classOf[PartnerUserClientCredentials].isAssignableFrom(parameter.getParameterType)

    protected val redirectPath = Option("/partner/login")

    protected val requestAttribute = "partnerUserClientCredentials"

    protected def resolveCredentials(request: HttpServletRequest) =
            cookieManager.findPartnerUserCookie(request).map { puc =>
                new PartnerUserClientCredentials with EchoedClientCredentials { val id = puc }
            }
}
