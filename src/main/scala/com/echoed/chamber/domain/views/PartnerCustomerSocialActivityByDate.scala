package com.echoed.chamber.domain.views
import java.util.{List => JList}


case class PartnerCustomerSocialActivityByDate(
        partnerId: String,
        echoedUserId: String,
        series: JList[SocialActivityHistory])