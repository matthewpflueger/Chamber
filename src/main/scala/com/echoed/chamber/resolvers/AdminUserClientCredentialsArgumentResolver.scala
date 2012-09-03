package com.echoed.chamber.resolvers

import org.springframework.core.MethodParameter
import javax.servlet.http.HttpServletRequest
import com.echoed.chamber.services.EchoedClientCredentials
import com.echoed.chamber.services.adminuser.AdminUserClientCredentials

class AdminUserClientCredentialsArgumentResolver extends ClientCredentialsArgumentResolver {

    def supportsParameter(parameter: MethodParameter) =
        classOf[AdminUserClientCredentials].isAssignableFrom(parameter.getParameterType)

    protected val redirectPath = Option("/admin/login")

    protected val requestAttribute = "adminUserClientCredentials"

    protected def resolveCredentials(request: HttpServletRequest) =
            cookieManager
                .findAdminUserCookie(request)
                .map { auc => new AdminUserClientCredentials with EchoedClientCredentials { val id = auc } }

}
