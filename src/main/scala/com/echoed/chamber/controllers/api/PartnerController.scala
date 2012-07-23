package com.echoed.chamber.controllers.api

import org.springframework.stereotype.Controller
import com.echoed.chamber.services.partneruser._
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod,ResponseBody,PathVariable}
import com.echoed.chamber.controllers.{EchoedController, ErrorResult}
import org.springframework.web.context.request.async.DeferredResult


@Controller
@RequestMapping(Array("/partner"))
class PartnerController extends EchoedController {

    @RequestMapping(value = Array("/settings"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getSettings(pucc: PartnerUserClientCredentials) = {
        val result = new DeferredResult(ErrorResult.timeout)

        mp(GetPartnerSettings(pucc)).onSuccess {
            case GetPartnerSettingsResponse(_, Right(partnerSettings)) =>
                log.debug("Successfully received partnerSettings for {}", pucc)
                result.set(partnerSettings)
        }

        result
    }

    @RequestMapping(value = Array("/products/{id}/{query}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getJSON(
            @PathVariable(value = "id") productId: String,
            @PathVariable(value = "query") query: String,
            pucc: PartnerUserClientCredentials) = {

        val result = new DeferredResult(ErrorResult.timeout)

        query match {
            case "history" =>
                mp(GetProductSocialActivityByDate(pucc, productId)).onSuccess {
                    case GetProductSocialActivityByDateResponse(_, Right(socialActivityHistory)) =>
                        result.set(socialActivityHistory)
                }
            case "summary" =>
                mp(GetProductSocialSummary(pucc, productId)).onSuccess {
                    case GetProductSocialSummaryResponse(_, Right(productSocialSummary))=>
                        result.set(productSocialSummary)
                }
            case "comments" =>
                mp(GetCommentsByProductId(pucc, productId)).onSuccess {
                    case GetCommentsByProductIdResponse(_, Right(productComments)) =>
                        result.set(productComments)
                }
        }

        result

    }

    @RequestMapping(value = Array("/customers/{id}/{query}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getCustomerJSON(
            @PathVariable(value="id") customerId: String,
            @PathVariable(value="query") query: String,
            pucc: PartnerUserClientCredentials) = {

        val result = new DeferredResult(ErrorResult.timeout)

        query match {
            case "summary" =>
                mp(GetCustomerSocialSummary(pucc, customerId)).onSuccess {
                    case GetCustomerSocialSummaryResponse(_, Right(customerSocialSummary)) =>
                        result.set(customerSocialSummary)
                }
            case "history" =>
                mp(GetCustomerSocialActivityByDate(pucc, customerId)).onSuccess {
                    case GetCustomerSocialActivityByDateResponse(_, Right(socialActivityHistory)) =>
                        result.set(socialActivityHistory)
                }
        }

        result
    }

    @RequestMapping(value = Array("/reports/{query}"), method = Array(RequestMethod.GET))
    @ResponseBody
    def getPartnerJSON(
            @PathVariable(value="query") query: String,
            pucc: PartnerUserClientCredentials) = {

        val result = new DeferredResult(ErrorResult.timeout)

        query match {
            case "summary" =>
                mp(GetPartnerSocialSummary(pucc)).onSuccess {
                    case GetPartnerSocialSummaryResponse(_, Right(partnerSocialSummary)) =>
                        log.debug("Partner Social Summary: {}", partnerSocialSummary)
                        result.set(partnerSocialSummary)
                }
            case "echoes" =>
                mp(GetEchoes(pucc)).onSuccess {
                    case GetEchoesResponse(_, Right(echoes)) => result.set(echoes)
                }
            case "history" =>
                mp(GetPartnerSocialActivityByDate(pucc)).onSuccess {
                    case GetPartnerSocialActivityByDateResponse(_, Right(partnerSocialActivity)) =>
                        result.set(partnerSocialActivity)
                }
            case "comments" =>
                mp(GetComments(pucc)).onSuccess{
                    case GetCommentsResponse(_, Right(comments)) => result.set(comments)
                }
            case "customers" =>
                mp(GetCustomers(pucc)).onSuccess {
                    case GetCustomersResponse(_, Right(customers)) => result.set(customers)
                }
            case "topcustomers" =>
                mp(GetTopCustomers(pucc)).onSuccess {
                    case GetTopCustomersResponse(_, Right(customers)) => result.set(customers)
                }
            case "products" =>
                mp(GetProducts(pucc)).onSuccess {
                    case GetProductsResponse(_, Right(products))=> result.set(products)
                }
            case "topproducts" =>
                mp(GetTopProducts(pucc)).onSuccess {
                    case GetTopProductsResponse(_, Right(products)) => result.set(products)
                }
            case "geolocation" =>
                mp(GetEchoClickGeoLocation(pucc)).onSuccess {
                    case GetEchoClickGeoLocationResponse(_, Right(geolocation)) => result.set(geolocation)
                }
        }

        result
    }

}
