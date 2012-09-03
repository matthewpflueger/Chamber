package com.echoed.chamber.resolvers

import org.springframework.core.MethodParameter
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

trait PreflightArgumentResolver {

    def preflightResolveArgument(
            parameter: MethodParameter,
            request: HttpServletRequest,
            response: HttpServletResponse): Boolean

}
