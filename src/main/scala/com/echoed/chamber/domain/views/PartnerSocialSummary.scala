package com.echoed.chamber.domain.views


case class PartnerSocialSummary(
    partnerId:String,
    partnerName: String,
    totalEchoes: Int,
    totalFacebookLikes: Int,
    totalFacebookComments: Int,
    totalEchoClicks: Int,
    totalSales: Float,
    totalSalesVolume: Int
)
