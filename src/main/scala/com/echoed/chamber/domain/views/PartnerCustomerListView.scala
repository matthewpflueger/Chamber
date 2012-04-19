package com.echoed.chamber.domain.views
import java.util.{ArrayList, List => JList}


case class PartnerCustomerListView(
    partnerId: String,
    partnerName: String,
    customers: JList[CustomerSocialSummary]){

    def this(partnerId: String, partnerName: String) = this(partnerId, partnerName, new ArrayList[CustomerSocialSummary])
}