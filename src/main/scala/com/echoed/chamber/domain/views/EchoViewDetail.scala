package com.echoed.chamber.domain.views

import java.util.Date


case class EchoViewDetail(
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
                        echoedUserId: String,
                        echoedUserName: String,
                        retailerId: String,
                        retailerName: String,
                        retailerSettingsId: String,
                        retailerSettingsMinClicks: Int,
                        retailerSettingsMinPercentage: Float,
                        retailerSettingsMaxPercentage: Float,
                        facebookPostId: String,
                        facebookPostFacebookId: String,
                        twitterStatusId: String,
                        twitterStatusTwitterId: String)

