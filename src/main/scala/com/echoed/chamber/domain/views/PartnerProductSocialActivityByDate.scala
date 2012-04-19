package com.echoed.chamber.domain.views
import java.util.{List => JList}


case class PartnerProductSocialActivityByDate(
    partnerId: String,
    productId: String,
    series: JList[SocialActivityHistory]
){
}