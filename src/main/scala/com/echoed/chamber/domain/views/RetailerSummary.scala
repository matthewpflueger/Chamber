package com.echoed.chamber.domain.views

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 1/3/12
 * Time: 1:41 PM
 * To change this template use File | Settings | File Templates.
 */

case class RetailerSummary(
    retailerId:String,
    retailerName: String,
    totalEchoes: Int,
    totalFacebookLikes: Int,
    totalFacebookComments: Int
)
