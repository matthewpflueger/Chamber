package com.echoed.chamber.domain.views

import java.util.Date

/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 2/9/12
 * Time: 5:44 PM
 * To change this template use File | Settings | File Templates.
 */

case class EchoViewPersonal(
       echoId: String,
       echoBoughtOn: Date,
       echoImageUrl: String,
       echoProductName: String,
       echoCategory: String,
       echoBrand: String,
       echoPrice: Float,
       echoLandingPageUrl: String,
       echoTotalClicks: Int,
       echoCredit: Float,
       echoCreditWindowEndsAt: Date,
       retailerName: String,
       retailerSettingsMinClicks: Int,
       retailerSettingsMinPercentage: Float){
    
    def this(echoView: EchoView ) = this(
                                        echoView.echoId,
                                        echoView.echoBoughtOn,
                                        echoView.echoImageUrl,
                                        echoView.echoProductName,
                                        echoView.echoCategory,
                                        echoView.echoBrand,
                                        echoView.echoPrice,
                                        echoView.echoLandingPageUrl,
                                        echoView.echoTotalClicks,
                                        echoView.echoCredit, 
                                        echoView.echoCreditWindowEndsAt,
                                        echoView.retailerName,
                                        echoView.retailerSettingsMinClicks,
                                        echoView.retailerSettingsMinPercentage)
}
