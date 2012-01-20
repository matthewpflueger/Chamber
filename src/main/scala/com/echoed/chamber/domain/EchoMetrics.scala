package com.echoed.chamber.domain

import java.lang.{Math => JMath}
import java.util.{GregorianCalendar, Calendar, UUID, Date}


case class EchoMetrics(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        echoId: String,
        echoedUserId: String,
        retailerId: String,
        retailerSettingsId: String,
        price: Float,
        creditWindowEndsAt: Date,
        totalClicks: Int,
        clicks: Int,
        credit: Float,
        fee: Float,
        residualClicks: Int,
        residualCredit: Float,
        residualFee: Float) {

    def this(echo: Echo, retailerSettings: RetailerSettings) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        echo.id,
        echo.echoedUserId,
        echo.retailerId,
        echo.retailerSettingsId,
        echo.price,
        retailerSettings.creditWindowEndsAt,
        0,
        0,
        0,
        0,
        0,
        0,
        0)

    def echoed(rs: RetailerSettings) = {
        require(retailerSettingsId == rs.id)
        require(credit == 0)
        require(fee == 0)
        require(totalClicks == 0)

        val (cr, fe) = update(0, rs.closetPercentage, rs.echoedMaxPercentage, rs.echoedMatchPercentage)
        this.copy(credit = cr, fee = fe)
    }

    def clicked(rs: RetailerSettings): EchoMetrics = {
        clicked(rs, creditWindowEndsAt.after(new Date))
    }

    /* Only for testing... */
    protected[domain] def clicked(rs: RetailerSettings, withinCreditWindow: Boolean): EchoMetrics = {
        require(retailerSettingsId == rs.id)

        val totalClicks = this.totalClicks + 1

        def withinWindow(tuple: Tuple2[Float, Float]) = this.copy(
                totalClicks = totalClicks,
                clicks = clicks + 1,
                credit = tuple._1,
                fee = tuple._2)
        def outsideWindow(tuple: Tuple2[Float, Float]) = this.copy(
                totalClicks = totalClicks,
                residualClicks = residualClicks + 1,
                residualCredit = tuple._1,
                residualFee = tuple._2)

        val updateForWindow = if (withinCreditWindow) withinWindow _ else outsideWindow _

        if (totalClicks < 1 || totalClicks < rs.minClicks || totalClicks > rs.maxClicks) {
            this.copy(totalClicks = totalClicks) //if we are under the min or over the max we don't track...
        } else if (totalClicks == rs.minClicks) {
            updateForWindow(update(totalClicks, rs.minPercentage, rs.echoedMaxPercentage, rs.echoedMatchPercentage))
        } else if (totalClicks == rs.maxClicks) {
            updateForWindow(update(totalClicks, rs.maxPercentage, rs.echoedMaxPercentage, rs.echoedMatchPercentage))
        } else {
            val percentage = logOfBase(rs.maxClicks, totalClicks - rs.minClicks) * (rs.maxPercentage - rs.minPercentage) + rs.minPercentage
            updateForWindow(update(totalClicks, percentage, rs.echoedMaxPercentage, rs.echoedMatchPercentage))
        }
    }


    private def update(
            totalClicks: Int,
            percentage: Float,
            echoedMaxPercentage: Float,
            echoedMatchPercentage: Float) = {

        val credit = price * percentage
        val fee =
            if (echoedMaxPercentage < percentage) {
                price * echoedMaxPercentage
            } else {
                credit * echoedMatchPercentage
            }
        (credit, fee)
    }

    private def logOfBase(base: Int, number: Int) = (JMath.log(number) / JMath.log(base)).floatValue()
}


