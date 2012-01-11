package com.echoed.chamber.domain.views


import java.util.Date
/**
 * Created by IntelliJ IDEA.
 * User: jonlwu
 * Date: 1/11/12
 * Time: 10:19 AM
 * To change this template use File | Settings | File Templates.
 */

case class EchoViewPublic(
                         echoId: String,
                         echoBoughtOn: Date,
                         echoImageUrl: String,
                         echoProductName: String,
                         echoCategory: String,
                         echoBrand: String,
                         echoLandingPageUrl: String,
                         retailerName: String
)

