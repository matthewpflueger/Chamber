package com.echoed.chamber.resolvers

import org.springframework.web.method.support.{ModelAndViewContainer, HandlerMethodArgumentResolver}
import org.springframework.beans.factory.annotation.Autowired
import com.echoed.chamber.controllers.CookieManager
import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.bind.support.WebDataBinderFactory
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.echoed.chamber.services.EchoedClientCredentials
import javax.annotation.Nullable
import com.echoed.util.Encrypter

abstract class ClientCredentialsArgumentResolver
        extends HandlerMethodArgumentResolver
        with PreflightArgumentResolver {

    @Autowired var encrypter: Encrypter = _
    @Autowired var cookieManager: CookieManager = _


    def resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory) =
        resolve(parameter, webRequest.getNativeRequest(classOf[HttpServletRequest])).fold(throw _, { c => c })

    protected val redirectPath: Option[String]

    protected def handleResolveFailure(
            e: IllegalArgumentException,
            request: HttpServletRequest,
            response: HttpServletResponse) {
        if (request.getMethod == "GET" && redirectPath.isDefined) response.sendRedirect(redirectPath.get)
        else response.sendError(401, "No credentials")
    }


    def preflightResolveArgument(
            parameter: MethodParameter,
            request: HttpServletRequest,
            response: HttpServletResponse) =
        resolve(parameter, request).fold(
            e => { handleResolveFailure(e, request, response); false },
            { _ => true })


    protected val requestAttribute: String

    protected def resolveCredentials(request: HttpServletRequest): Option[EchoedClientCredentials]

    protected def resolve(
            parameter: MethodParameter,
            request: HttpServletRequest): Either[IllegalArgumentException, EchoedClientCredentials] =
        Option(request.getAttribute(requestAttribute))
            .map(_.asInstanceOf[EchoedClientCredentials])
            .orElse {
                resolveCredentials(request).orElse {
                    if (parameter.hasParameterAnnotation(classOf[Nullable])) Some(null) //null is a valid argument
                    else None
                }
            }.map { ecc =>
                request.setAttribute(requestAttribute, ecc)
                ecc
            }.toRight(new IllegalArgumentException("Cannot resolve credentials"))

}

