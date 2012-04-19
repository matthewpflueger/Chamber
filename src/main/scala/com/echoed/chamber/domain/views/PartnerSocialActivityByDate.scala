package com.echoed.chamber.domain.views
import java.util.{ArrayList, List => JList}


case class PartnerSocialActivityByDate(
       partnerId: String,
       series: JList[SocialActivityHistory]){


}