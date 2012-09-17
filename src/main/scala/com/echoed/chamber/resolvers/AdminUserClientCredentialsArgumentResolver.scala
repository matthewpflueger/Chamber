package com.echoed.chamber.resolvers

import org.springframework.core.MethodParameter
import javax.servlet.http.HttpServletRequest
import com.echoed.chamber.services.EchoedClientCredentials
import com.echoed.chamber.services.adminuser.AdminUserClientCredentials
import com.echoed.util.ScalaObjectMapper

class AdminUserClientCredentialsArgumentResolver extends ClientCredentialsArgumentResolver {

    def supportsParameter(parameter: MethodParameter) =
        classOf[AdminUserClientCredentials].isAssignableFrom(parameter.getParameterType)

    protected val redirectPath = Option("/admin/login")

    protected val requestAttribute = "adminUserClientCredentials"

    protected def resolveCredentials(request: HttpServletRequest) =
        cookieManager.findAdminUserCookie(request).map { auc =>
                val payload = new ScalaObjectMapper().readTree(encrypter.decrypt(auc))
                AdminUserClientCredentials(
                        payload.get("id").asText,
                        Option(payload.get("name")).map(_.asText),
                        Option(payload.get("email")).map(_.asText))
            }

}
