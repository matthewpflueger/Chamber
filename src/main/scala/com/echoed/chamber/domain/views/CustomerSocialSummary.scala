package com.echoed.chamber.domain.views

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 1/6/12
 * Time: 10:42 AM
 * To change this template use File | Settings | File Templates.
 */

case class CustomerSocialSummary(
    echoedUserId: String, 
    echoedUserName: String, 
    totalEchoes: Int, 
    totalFacebookLikes: Int, 
    totalFacebookComments: Int, 
    totalEchoClicks: Int,
    totalFacebookFriends: Int,
    totalSales: Float,
    totalSalesVolume: Int
){
    def this(echoedUserId: String,
             echoedUserName:String,
             totalEchoes: Int,
             totalFacebookLikes: Int, 
             totalFacebookComments: Int, 
             totalEchoClicks: Int,
             totalFacebookFriends: Int) = this(echoedUserId,echoedUserName,totalEchoes,totalFacebookLikes,totalFacebookComments,totalEchoClicks,totalFacebookFriends,0,0)
}