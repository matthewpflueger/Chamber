package com.echoed.chamber.domain

import java.lang.{Math => JMath}
import java.util.{UUID, Date}
import partner.PartnerSettings


case class EchoMetrics(
        id: String,
        updatedOn: Date,
        createdOn: Date,
        echoId: String,
        echoedUserId: String,
        partnerId: String,
        partnerSettingsId: String,
        price: Float,
        creditWindowEndsAt: Date,
        totalClicks: Int,
        clicks: Int,
        credit: Float,
        fee: Float,
        residualClicks: Int,
        residualCredit: Float,
        residualFee: Float) {

    def this(echo: Echo, partnerSettings: PartnerSettings) = this(
        UUID.randomUUID.toString,
        new Date,
        new Date,
        echo.id,
        echo.echoedUserId,
        echo.partnerId,
        echo.partnerSettingsId,
        echo.price,
        null,
        0,
        0,
        0,
        0,
        0,
        0,
        0)

    val isEchoed = echoedUserId != null && creditWindowEndsAt != null

    def echoed(rs: PartnerSettings) = {
        require(partnerSettingsId == rs.id)
        require(credit == 0)
        require(fee == 0)
        require(creditWindowEndsAt == null)

        val (cr, fe) = update(rs.closetPercentage, rs.echoedMaxPercentage, rs.echoedMatchPercentage)
        this.copy(credit = cr, fee = fe, creditWindowEndsAt = rs.creditWindowEndsAt)
    }

    def clicked(rs: PartnerSettings): EchoMetrics = {
        require(partnerSettingsId == rs.id)

        if (isEchoed) {
            clicked(rs, creditWindowEndsAt.after(new Date))
        } else if (totalClicks == 0) {
            val (cr, fe) = update(rs.closetPercentage, rs.echoedMaxPercentage, rs.echoedMatchPercentage)
            this.copy(residualCredit = cr, residualFee = fe, totalClicks = 1, residualClicks = 1)
        } else {
            clicked(rs, false)
        }
    }

    /* package visible for testing purposes otherwise should be private... */
    protected[domain] def clicked(rs: PartnerSettings, withinCreditWindow: Boolean): EchoMetrics = {
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
            //if we are under the min or over the max we don't track credit or fee...
            if (withinCreditWindow) updateForWindow((credit, fee)) else updateForWindow((residualCredit, residualFee))
        } else if (totalClicks == rs.minClicks) {
            updateForWindow(update(rs.minPercentage, rs.echoedMaxPercentage, rs.echoedMatchPercentage))
        } else if (totalClicks == rs.maxClicks) {
            updateForWindow(update(rs.maxPercentage, rs.echoedMaxPercentage, rs.echoedMatchPercentage))
        } else {
            val percentage = (scala.math.pow(logOfBase(rs.maxClicks, totalClicks - rs.minClicks), 3) * (rs.maxPercentage - rs.minPercentage) + rs.minPercentage).asInstanceOf[Float]
            updateForWindow(update(percentage, rs.echoedMaxPercentage, rs.echoedMatchPercentage))
        }
    }

    private def update(
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


