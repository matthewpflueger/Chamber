package com.echoed.chamber.domain.views
import java.util.{ArrayList, List => JList}

case class PartnerProductsListView(
    partnerId: String,
    partnerName: String,
    products: JList[ProductSocialSummary]){

    def this(partnerId: String, partnerName: String) = this(partnerId, partnerName, new ArrayList[ProductSocialSummary])
}