package com.echoed.chamber.domain

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, Spec}
import org.scalatest.matchers.ShouldMatchers
import java.util.Date


@RunWith(classOf[JUnitRunner])
class EchoMetricsTest extends Spec with GivenWhenThen with ShouldMatchers {

    var retailerSettings = new RetailerSettings(
            retailerId = "retailerId",
            closetPercentage = 0.01f,
            minClicks = 1,
            minPercentage = 0.1f,
            maxClicks = 10,
            maxPercentage = 0.2f,
            echoedMatchPercentage = 1f,
            echoedMaxPercentage = 0.2f,
            activeOn = new Date,
            creditWindow = 1)

    var echo = Echo.make(
            retailerId = "retailerId",
            customerId = "customerId",
            productId = "productId",
            boughtOn = new Date,
            orderId = "orderId",
            price = 10.00f,
            imageUrl = "imageUrl",
            landingPageUrl = "landingPageUrl",
            productName= "My Awesome Boots",
            category= "Footwear",
            brand = "Nike",
            description = "These are amazing boots",
            echoClickId = null,
            step = "test")
    echo = echo.copy(
            echoedUserId = "echoedUserId",
            facebookPostId = "facebookPostId",
            twitterStatusId = "twitterStatusId",
            echoPossibilityId = "echoPossibilityId",
            retailerSettingsId = retailerSettings.id)

    var echoMetrics = new EchoMetrics(echo, retailerSettings)
    echo = echo.copy(echoMetricsId = echoMetrics.id)

    def verifyZeroResidual(em: EchoMetrics) {
        em.residualClicks should be(0)
        em.residualCredit should be(0)
        em.residualFee should be (0)
    }

    describe("An Echo") {

        it("should calculate the credit and fee for being echoed") {
            given("a new Echo")
            when("not echoed before")
            then("it should calculate the credit and fee when echoed")
            echoMetrics = echoMetrics.echoed(retailerSettings)
            echoMetrics.totalClicks should be (0)
            echoMetrics.clicks should be (0)
            echoMetrics.credit should be (0.1f plusOrMinus 0.01f)
            echoMetrics.fee should be (0.1f plusOrMinus 0.01f)

            verifyZeroResidual(echoMetrics)
        }

        it("cannot be echoed twice") {
            given("an already echoed Echo")
            when("echoed again")
            then("it should throw an exception")
            evaluating { echoMetrics.echoed(retailerSettings) } should produce [IllegalArgumentException]
        }

        it("should not calculate the credit and fee when under the min clicks") {
            given("an Echo below the min number of clicks")
            when("clicked")
            then("it should not calculate the credit and fee of the click")
            var rs = retailerSettings.copy(minClicks = 2)
            var em = echoMetrics.copy()
            em = em.clicked(rs)
            em.totalClicks should be (1)

            em.clicks should be(0)
            em.credit should be (0.1f plusOrMinus 0.01f) //not changed from initial echo
            em.fee should be (0.1f plusOrMinus 0.01f)

            verifyZeroResidual(em)
        }

        it("should calculate the credit and fee for being clicked") {
            given("an Echo")
            when("clicked")
            then("it should calculate the credit and fee of the click")
            echoMetrics = echoMetrics.clicked(retailerSettings)
            echoMetrics.totalClicks should be (1)
            echoMetrics.clicks should be (echoMetrics.totalClicks - retailerSettings.minClicks + 1)
            echoMetrics.credit should be (1)
            echoMetrics.fee should be (1)

            verifyZeroResidual(echoMetrics)
        }

        it("should calculate the credit and fee up to max clicks when clicked") {
            given("an Echo")
            when("clicked multiple times")
            then("it should calculate the credit and fee of the click up to max clicks")
            echoMetrics.totalClicks should be (1)
            echoMetrics.clicks should be (echoMetrics.totalClicks - retailerSettings.minClicks + 1)
            echoMetrics.credit should be (1)
            echoMetrics.fee should be (1)

            verifyZeroResidual(echoMetrics)

            echoMetrics = echoMetrics.clicked(retailerSettings).clicked(retailerSettings) //click to get it past the min percentage

            var clicks = echoMetrics.clicks
            var credit = echoMetrics.credit
            var fee = echoMetrics.fee

            for (num <- echoMetrics.totalClicks + 1 to retailerSettings.maxClicks) {
                echoMetrics = echoMetrics.clicked(retailerSettings)
                echoMetrics.totalClicks should be (num)
                echoMetrics.clicks should be (clicks + 1)
                echoMetrics.credit should be > credit
                echoMetrics.fee should be > fee
                credit = echoMetrics.credit
                fee = echoMetrics.fee
                clicks = echoMetrics.clicks
            }

            echoMetrics.totalClicks should equal (retailerSettings.maxClicks)
            echoMetrics.clicks should be (clicks)
            echoMetrics.credit should be (2f)
            echoMetrics.fee should be (2f)

            for (num <- echoMetrics.totalClicks + 1 to (echoMetrics.totalClicks + 5)) {
                echoMetrics = echoMetrics.clicked(retailerSettings)
                echoMetrics.totalClicks should be (num)
                echoMetrics.clicks should be (clicks)
                echoMetrics.credit should be (2f)
                echoMetrics.fee should be (2f)
            }

            verifyZeroResidual(echoMetrics)
        }

        it("should calculate the fee up to max echoed percentage when clicked") {
            given("an Echo")
            when("clicked multiple times")
            then("it should calculate the fee up to max echoed percentage")
            retailerSettings = retailerSettings.copy(echoedMaxPercentage = 0.15f)
            echoMetrics = echoMetrics.copy(totalClicks = 0, clicks = 0, credit = 0, fee = 0, creditWindowEndsAt = null)

            echoMetrics = echoMetrics.echoed(retailerSettings)

            for (num <- echoMetrics.totalClicks + 1 to retailerSettings.maxClicks)
                echoMetrics = echoMetrics.clicked(retailerSettings)

            echoMetrics.totalClicks should equal (retailerSettings.maxClicks)
            echoMetrics.clicks should be (echoMetrics.totalClicks - retailerSettings.minClicks + 1)
            echoMetrics.credit should be (2f)
            echoMetrics.fee should be (1.5f)

            val clicks = echoMetrics.clicks

            for (num <- echoMetrics.totalClicks + 1 to (echoMetrics.totalClicks + 5)) {
                echoMetrics = echoMetrics.clicked(retailerSettings)
                echoMetrics.totalClicks should be (num)
                echoMetrics.clicks should be (clicks)
                echoMetrics.credit should be (2f)
                echoMetrics.fee should be (1.5f)
            }

            verifyZeroResidual(echoMetrics)
        }

        it("should calculate the fee using echoed match percentage up to echoed max percentage when clicked") {
            given("an Echo")
            when("clicked multiple times")
            then("it should calculate the fee using echoed match percentage up to echoed max percentage ")
            retailerSettings = retailerSettings.copy(echoedMaxPercentage = 0.18f, echoedMatchPercentage = 0.8f)
            echoMetrics = echoMetrics.copy(totalClicks = 0, clicks = 0, credit = 0, fee = 0, creditWindowEndsAt = null)

            echoMetrics = echoMetrics.echoed(retailerSettings)
            echoMetrics.totalClicks should be (0)
            echoMetrics.clicks should be (0)
            echoMetrics.credit should be (0.1f plusOrMinus 0.01f)
            echoMetrics.fee should be (0.08f plusOrMinus 0.001f)


            echoMetrics = echoMetrics.clicked(retailerSettings).clicked(retailerSettings) //click to get it past the min percentage
            echoMetrics.totalClicks should be (2)
            echoMetrics.clicks should be (echoMetrics.totalClicks - retailerSettings.minClicks + 1)

            var clicks = echoMetrics.clicks
            var credit = echoMetrics.credit
            var fee = echoMetrics.fee

            fee should be < (credit)

            for (num <- echoMetrics.totalClicks + 1 to retailerSettings.maxClicks) {
                echoMetrics = echoMetrics.clicked(retailerSettings)
                echoMetrics.totalClicks should be (num)
                echoMetrics.clicks should be (echoMetrics.totalClicks - retailerSettings.minClicks + 1)
                echoMetrics.credit should be > credit
                echoMetrics.fee should be < echoMetrics.credit
                credit = echoMetrics.credit
                fee = echoMetrics.fee
                clicks = echoMetrics.clicks
            }

            echoMetrics.totalClicks should equal (retailerSettings.maxClicks)
            echoMetrics.clicks should be (echoMetrics.totalClicks - retailerSettings.minClicks + 1)
            echoMetrics.credit should be (2f)
            echoMetrics.fee should be (1.8f plusOrMinus 0.01f)

            verifyZeroResidual(echoMetrics)

            for (num <- echoMetrics.totalClicks + 1 to (echoMetrics.totalClicks + 5)) {
                echoMetrics = echoMetrics.clicked(retailerSettings)
                echoMetrics.totalClicks should be (num)
                echoMetrics.clicks should be (clicks)
                echoMetrics.credit should be (2f)
                echoMetrics.fee should be (1.8f plusOrMinus 0.01f)
            }

            verifyZeroResidual(echoMetrics)
        }


        it("should calculate the credit and fee only within the credit window") {
            given("an Echo")
            when("clicked multiple times within the credit window")
            then("it should calculate the credit and fee only when in the credit window")
            retailerSettings = retailerSettings.copy(echoedMaxPercentage = 0.2f, echoedMatchPercentage = 1f)
            echoMetrics = echoMetrics.copy(totalClicks = 0, clicks = 0, credit = 0, fee = 0, creditWindowEndsAt = null)

            echoMetrics = echoMetrics.echoed(retailerSettings)
            echoMetrics.totalClicks should be (0)
            echoMetrics.credit should be (0.1f plusOrMinus 0.01f)
            echoMetrics.fee should be (0.1f plusOrMinus 0.01f)

            echoMetrics = echoMetrics.clicked(retailerSettings).clicked(retailerSettings) //click to get it past the min percentage
            echoMetrics.totalClicks should be (2)
            echoMetrics.clicks should be (echoMetrics.totalClicks - retailerSettings.minClicks + 1)
            echoMetrics.credit should be (1)
            echoMetrics.fee should be (1)

            verifyZeroResidual(echoMetrics)

            var clicks = echoMetrics.clicks
            var credit = echoMetrics.credit
            var fee = echoMetrics.fee

            for (num <- echoMetrics.totalClicks + 1 to (retailerSettings.maxClicks - 5)) {
                echoMetrics = echoMetrics.clicked(retailerSettings)
                echoMetrics.totalClicks should be (num)
                echoMetrics.clicks should be (echoMetrics.totalClicks - retailerSettings.minClicks + 1)
                echoMetrics.credit should be > credit
                echoMetrics.fee should be > fee
                credit = echoMetrics.credit
                fee = echoMetrics.fee
                clicks = echoMetrics.clicks
            }

            echoMetrics.clicks should equal (clicks)
            echoMetrics.credit should be (1.6f plusOrMinus 0.01f)
            echoMetrics.fee should be (1.6f plusOrMinus 0.01f)

            verifyZeroResidual(echoMetrics)

            var residualCredit, residualFee = 0f
            var residualClicks = 0

            for (num <- echoMetrics.totalClicks + 1 to retailerSettings.maxClicks) {
                echoMetrics = echoMetrics.clicked(retailerSettings, false) //clicks no longer within window...
                echoMetrics.totalClicks should be (num)
                echoMetrics.residualClicks should be (residualClicks + 1)
                echoMetrics.residualCredit should be > residualCredit
                echoMetrics.residualCredit should be > echoMetrics.credit
                echoMetrics.residualFee should be > residualFee
                echoMetrics.residualFee should be > echoMetrics.fee

                echoMetrics.clicks should be (clicks)
                echoMetrics.fee should be (fee)
                echoMetrics.credit should be (credit)

                residualCredit = echoMetrics.residualCredit
                residualFee = echoMetrics.residualFee
                residualClicks = echoMetrics.residualClicks
            }


            echoMetrics.clicks should equal (clicks)
            echoMetrics.credit should be (credit)
            echoMetrics.fee should be (fee)

            echoMetrics.residualClicks should be (residualClicks)
            echoMetrics.residualCredit should be (2f)
            echoMetrics.residualCredit should be > echoMetrics.credit
            echoMetrics.residualFee should be (2f)
            echoMetrics.residualFee should be > echoMetrics.fee
        }
    }
}
