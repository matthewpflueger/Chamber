package com.echoed.chamber.domain.views


case class ProductSocialSummary(
    productId: String, 
    productName: String,
    productBrand: String,
    productCategory: String,
    partnerId: String,
    partnerName: String,
    productImageUrl: String,
    totalEchoes: Int,
    totalFacebookLikes: Int,
    totalFacebookComments: Int,
    totalEchoClicks: Int,
    totalSales: Float,
    totalSalesVolume: Int
)
