package com.echoed.chamber.resolvers

import org.springframework.core.MethodParameter
import javax.servlet.http.HttpServletRequest
import com.echoed.chamber.services.adminuser.AdminUserClientCredentials
import com.echoed.util.ScalaObjectMapper

class AdminUserClientCredentialsArgumentResolver extends ClientCredentialsArgumentResolver {

    def supportsParameter(parameter: MethodParameter) =
        classOf[AdminUserClientCredentials].isAssignableFrom(parameter.getParameterType)

    protected val redirectPath = Option("/admin/login")

    protected val requestAttribute = "adminUserClientCredentials"

    protected def resolveCredentials(request: HttpServletRequest) =
        cookieManager.findAdminUserCookie(request).map { auc =>
                val payload = new ScalaObjectMapper()
                        .readValue(encrypter.decrypt(auc), classOf[Map[String, String]])
                        .filter(kv => kv._2 != null)
                AdminUserClientCredentials(
                        payload.get("id").get,
                        payload.get("name"),
                        payload.get("email"))
            }

}
