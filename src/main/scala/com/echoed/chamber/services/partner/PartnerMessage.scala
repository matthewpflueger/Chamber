package com.echoed.chamber.services.partner

import com.echoed.chamber.services.{EchoedException, ResponseMessage => RM, Message}
import com.echoed.chamber.domain.views.{RetailerSocialSummary, ProductSocialSummary, RetailerProductsListView, RetailerCustomerListView, RetailerProductSocialActivityByDate, RetailerSocialActivityByDate, CustomerSocialSummary, RetailerCustomerSocialActivityByDate}
import com.echoed.chamber.domain.{RetailerSettings, Retailer, RetailerUser, FacebookComment}


sealed trait PartnerMessage extends Message

sealed case class PartnerException(message: String = "", cause: Throwable = null) extends EchoedException(message, cause)

import com.echoed.chamber.services.partner.{PartnerMessage => PM}
import com.echoed.chamber.services.partner.{PartnerException => PE}


case class RegisterPartner(partner: Retailer, partnerSettings: RetailerSettings, partnerUser: RetailerUser) extends PM
case class RegisterPartnerResponse(
        message: RegisterPartner,
        value: Either[PE, Retailer]) extends PM with RM[Retailer, RegisterPartner, PE]




