package com.echoed.chamber.resolvers

import org.springframework.core.MethodParameter
import javax.servlet.http.HttpServletRequest
import com.echoed.chamber.services.EchoedClientCredentials
import com.echoed.chamber.services.partner.PartnerClientCredentials
import org.springframework.web.servlet.HandlerMapping
import java.util.{Map => JMap}


class PartnerClientCredentialsArgumentResolver extends ClientCredentialsArgumentResolver {

    def supportsParameter(parameter: MethodParameter) =
        classOf[PartnerClientCredentials].isAssignableFrom(parameter.getParameterType)

    protected val redirectPath = None

    protected val requestAttribute = "partnerClientCredentials"

    protected def resolveCredentials(request: HttpServletRequest) = {
        val variables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE).asInstanceOf[JMap[String, String]]

        Option(request.getParameter("pid")).orElse(Option(variables.get("pid"))).map { pid =>
            new PartnerClientCredentials with EchoedClientCredentials { val id = pid }
        }
    }
}
