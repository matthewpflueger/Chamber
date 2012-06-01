package com.echoed.chamber.domain

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, Spec}
import org.scalatest.matchers.ShouldMatchers
import java.util.Date
import partner.PartnerSettings


@RunWith(classOf[JUnitRunner])
class EchoMetricsTest extends Spec with GivenWhenThen with ShouldMatchers {

    var partnerSettings = new PartnerSettings(
            partnerId = "partnerId",
            closetPercentage = 0.01f,
            minClicks = 1,
            minPercentage = 0.1f,
            maxClicks = 10,
            maxPercentage = 0.2f,
            echoedMatchPercentage = 1f,
            echoedMaxPercentage = 0.2f,
            activeOn = new Date,
            creditWindow = 1,
            views = "echo.js.0, echo.js.1",
            hashTag = "@test")

    var zeroPartnerSettings = partnerSettings.copy(
            closetPercentage = 0f,
            minClicks = 0,
            minPercentage = 0f)

    var zeroZeroPartnerSettings = partnerSettings.copy(
            closetPercentage = 0f,
            minClicks = 0,
            minPercentage = 0f,
            maxClicks = 200,
            maxPercentage = 0f)

    var echo = Echo.make(
            partnerId = "partnerId",
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
            browserId = null,
            ipAddress = null,
            userAgent = null,
            referrerUrl = null,
            step = "test")
    echo = echo.copy(
            echoedUserId = "echoedUserId",
            facebookPostId = "facebookPostId",
            twitterStatusId = "twitterStatusId",
            echoPossibilityId = "echoPossibilityId",
            partnerSettingsId = partnerSettings.id)

    var echoMetrics = new EchoMetrics(echo, partnerSettings)
    echo = echo.copy(echoMetricsId = echoMetrics.id)

    var residualEchoMetrics = echoMetrics.copy()

    var zeroEchoMetrics = new EchoMetrics(echo, zeroPartnerSettings)
    var zeroResidualEchoMetrics = zeroEchoMetrics.copy()

    var zeroZeroEchoMetrics = new EchoMetrics(echo, zeroZeroPartnerSettings)

    def verifyZeroResidual(em: EchoMetrics) {
        em.residualClicks should be(0)
        em.residualCredit should be(0)
        em.residualFee should be (0)
    }

    def verifyZero(em: EchoMetrics) {
        em.clicks should be(0)
        em.credit should be(0)
        em.fee should be (0)
    }

    describe("An Echo") {

        it("should always be zero for a free echo") {
            given("a new free Echo")
            when("not echoed before")
            then("it should calculate 0 credit and fee when echoed for free")
            zeroZeroEchoMetrics = zeroZeroEchoMetrics.echoed(zeroZeroPartnerSettings)
            zeroZeroEchoMetrics.totalClicks should be (0)
            zeroZeroEchoMetrics.clicks should be (0)
            zeroZeroEchoMetrics.credit should be (0.0f)
            zeroZeroEchoMetrics.fee should be (0.0f)

            verifyZeroResidual(zeroZeroEchoMetrics)
        }

        it("should calculate the 0 credit and fee for being echoed for free") {
            given("a new Echo")
            when("not echoed before")
            then("it should calculate 0 credit and fee when echoed for free")
            zeroEchoMetrics = zeroEchoMetrics.echoed(zeroPartnerSettings)
            zeroEchoMetrics.totalClicks should be (0)
            zeroEchoMetrics.clicks should be (0)
            zeroEchoMetrics.credit should be (0.0f)
            zeroEchoMetrics.fee should be (0.0f)

            verifyZeroResidual(zeroEchoMetrics)
        }

        it("should calculate the credit and fee for being echoed") {
            given("a new Echo")
            when("not echoed before")
            then("it should calculate the credit and fee when echoed")
            echoMetrics = echoMetrics.echoed(partnerSettings)
            echoMetrics.totalClicks should be (0)
            echoMetrics.clicks should be (0)
            echoMetrics.credit should be (0.1f plusOrMinus 0.01f)
            echoMetrics.fee should be (0.1f plusOrMinus 0.01f)

            verifyZeroResidual(echoMetrics)
        }

        it("should calculate the 0 residual credit and residual fee for being clicked when not echoed") {
            given("a new Echo")
            when("not echoed before")
            then("it should calculate the 0 residual credit and residual fee when clicked")
            zeroResidualEchoMetrics = zeroResidualEchoMetrics.clicked(zeroPartnerSettings)
            zeroResidualEchoMetrics.totalClicks should be (1)
            zeroResidualEchoMetrics.residualClicks should be (1)
            zeroResidualEchoMetrics.residualCredit should be (0f)
            zeroResidualEchoMetrics.residualFee should be (0f)

            verifyZero(zeroResidualEchoMetrics)
        }

        it("should calculate the residual credit and residual fee for being clicked when not echoed") {
            given("a new Echo")
            when("not echoed before")
            then("it should calculate the residual credit and residual fee when clicked")
            residualEchoMetrics = residualEchoMetrics.clicked(partnerSettings)
            residualEchoMetrics.totalClicks should be (1)
            residualEchoMetrics.residualClicks should be (1)
            residualEchoMetrics.residualCredit should be (0.1f plusOrMinus 0.01f)
            residualEchoMetrics.residualFee should be (0.1f plusOrMinus 0.01f)

            verifyZero(residualEchoMetrics)
        }

        it("cannot be echoed twice") {
            given("an already echoed Echo")
            when("echoed again")
            then("it should throw an exception")
            evaluating { echoMetrics.echoed(partnerSettings) } should produce [IllegalArgumentException]
        }

        it("should not calculate the credit and fee when under the min clicks") {
            given("an Echo below the min number of clicks")
            when("clicked")
            then("it should not calculate the credit and fee of the click")
            var rs = partnerSettings.copy(minClicks = 2)
            var em = echoMetrics.copy()
            em = em.clicked(rs)
            em.totalClicks should be (1)

            em.clicks should be(1)
            em.credit should be (0.1f plusOrMinus 0.01f) //not changed from initial echo
            em.fee should be (0.1f plusOrMinus 0.01f)

            verifyZeroResidual(em)
        }

        it("should not calculate the residual credit and residual fee when under the min clicks") {
            given("an Echo below the min number of clicks")
            when("clicked")
            then("it should not calculate the residual credit and residual fee of the click")
            var rs = partnerSettings.copy(minClicks = 3)
            var em = residualEchoMetrics.copy()
            em = em.clicked(rs)
            em.totalClicks should be (2)

            em.residualClicks should be(2)
            em.residualCredit should be (0.1f plusOrMinus 0.01f) //not changed from initial echo
            em.residualFee should be (0.1f plusOrMinus 0.01f)

            verifyZero(em)
        }

        it("should calculate the 0 credit and fee for being clicked") {
            given("an Echo")
            when("clicked")
            then("it should calculate the 0 credit and fee of the click")
            0 until 5 foreach(_ => zeroEchoMetrics = zeroEchoMetrics.clicked(zeroPartnerSettings))
            zeroEchoMetrics.totalClicks should be (5)
            zeroEchoMetrics.clicks should be (5)
            zeroEchoMetrics.credit should be (0.68f plusOrMinus 0.01f)
            zeroEchoMetrics.fee should be (0.68f plusOrMinus 0.01f)

            verifyZeroResidual(zeroEchoMetrics)
        }

        it("should always be zero credit and fee for being clicked when free") {
            given("a free Echo")
            when("clicked")
            then("it should always be free")
            0 until 100 foreach(_ => zeroZeroEchoMetrics = zeroZeroEchoMetrics.clicked(zeroZeroPartnerSettings))
            zeroZeroEchoMetrics.totalClicks should be (100)
            zeroZeroEchoMetrics.clicks should be (100)
            zeroZeroEchoMetrics.credit should be (0f)
            zeroZeroEchoMetrics.fee should be (0f)

            verifyZeroResidual(zeroZeroEchoMetrics)
        }

        it("should calculate the credit and fee for being clicked") {
            given("an Echo")
            when("clicked")
            then("it should calculate the credit and fee of the click")
            echoMetrics = echoMetrics.clicked(partnerSettings)
            echoMetrics.totalClicks should be (1)
            echoMetrics.clicks should be (1)
            echoMetrics.credit should be (1)
            echoMetrics.fee should be (1)

            verifyZeroResidual(echoMetrics)
        }

        it("should calculate the residual credit and fee for being clicked") {
            given("an Echo")
            when("clicked and not echoed")
            then("it should calculate the residual credit and fee of the click")
            residualEchoMetrics = residualEchoMetrics.clicked(partnerSettings)
            residualEchoMetrics.totalClicks should be (2)
            residualEchoMetrics.residualClicks should be (2)
            residualEchoMetrics.residualCredit should be (1)
            residualEchoMetrics.residualFee should be (1)

            verifyZero(residualEchoMetrics)
        }

        it("should calculate the credit and fee up to max clicks when clicked") {
            given("an Echo")
            when("clicked multiple times")
            then("it should calculate the credit and fee of the click up to max clicks")
            echoMetrics.totalClicks should be (1)
            echoMetrics.clicks should be (1)
            echoMetrics.credit should be (1)
            echoMetrics.fee should be (1)

            verifyZeroResidual(echoMetrics)

            echoMetrics = echoMetrics.clicked(partnerSettings).clicked(partnerSettings) //click to get it past the min percentage

            var clicks = echoMetrics.clicks
            var credit = echoMetrics.credit
            var fee = echoMetrics.fee

            for (num <- echoMetrics.totalClicks + 1 to partnerSettings.maxClicks) {
                echoMetrics = echoMetrics.clicked(partnerSettings)
                echoMetrics.totalClicks should be (num)
                echoMetrics.clicks should be (clicks + 1)
                echoMetrics.credit should be > credit
                echoMetrics.fee should be > fee
                credit = echoMetrics.credit
                fee = echoMetrics.fee
                clicks = echoMetrics.clicks
            }

            echoMetrics.totalClicks should equal (partnerSettings.maxClicks)
            echoMetrics.clicks should be (clicks)
            echoMetrics.credit should be (2f)
            echoMetrics.fee should be (2f)

            for (num <- echoMetrics.totalClicks + 1 to (echoMetrics.totalClicks + 5)) {
                echoMetrics = echoMetrics.clicked(partnerSettings)
                echoMetrics.totalClicks should be (num)
                echoMetrics.clicks should be (num)
                echoMetrics.credit should be (2f)
                echoMetrics.fee should be (2f)
            }

            verifyZeroResidual(echoMetrics)
        }


        it("should calculate the residual credit and residual fee up to max clicks when clicked and not echoed") {
            given("an Echo")
            when("clicked multiple times when not echoed")
            then("it should calculate the residual credit and residual fee of the click up to max clicks")
            residualEchoMetrics.totalClicks should be (2)
            residualEchoMetrics.residualClicks should be (2)
            residualEchoMetrics.residualCredit should be (1)
            residualEchoMetrics.residualFee should be (1)

            verifyZero(residualEchoMetrics)

            residualEchoMetrics = residualEchoMetrics.clicked(partnerSettings).clicked(partnerSettings) //click to get it past the min percentage

            var clicks = residualEchoMetrics.residualClicks
            var credit = residualEchoMetrics.residualCredit
            var fee = residualEchoMetrics.residualFee

            for (num <- residualEchoMetrics.totalClicks + 1 to partnerSettings.maxClicks) {
                residualEchoMetrics = residualEchoMetrics.clicked(partnerSettings)
                residualEchoMetrics.totalClicks should be (num)
                residualEchoMetrics.residualClicks should be (clicks + 1)
                residualEchoMetrics.residualCredit should be > credit
                residualEchoMetrics.residualFee should be > fee
                credit = residualEchoMetrics.residualCredit
                fee = residualEchoMetrics.residualFee
                clicks = residualEchoMetrics.residualClicks
            }

            residualEchoMetrics.totalClicks should equal (partnerSettings.maxClicks)
            residualEchoMetrics.residualClicks should be (clicks)
            residualEchoMetrics.residualCredit should be (2f)
            residualEchoMetrics.residualFee should be (2f)

            for (num <- residualEchoMetrics.totalClicks + 1 to (residualEchoMetrics.totalClicks + 5)) {
                residualEchoMetrics = residualEchoMetrics.clicked(partnerSettings)
                residualEchoMetrics.totalClicks should be (num)
                residualEchoMetrics.residualClicks should be (num)
                residualEchoMetrics.residualCredit should be (2f)
                residualEchoMetrics.residualFee should be (2f)
            }

        }


        it("should calculate the fee up to max echoed percentage when clicked") {
            given("an Echo")
            when("clicked multiple times")
            then("it should calculate the fee up to max echoed percentage")
            partnerSettings = partnerSettings.copy(echoedMaxPercentage = 0.15f)
            echoMetrics = echoMetrics.copy(totalClicks = 0, clicks = 0, credit = 0, fee = 0, creditWindowEndsAt = null)

            echoMetrics = echoMetrics.echoed(partnerSettings)

            for (num <- echoMetrics.totalClicks + 1 to partnerSettings.maxClicks)
                echoMetrics = echoMetrics.clicked(partnerSettings)

            echoMetrics.totalClicks should equal (partnerSettings.maxClicks)
            echoMetrics.clicks should equal (echoMetrics.totalClicks)
            echoMetrics.credit should be (2f)
            echoMetrics.fee should be (1.5f)

            for (num <- echoMetrics.totalClicks + 1 to (echoMetrics.totalClicks + 5)) {
                echoMetrics = echoMetrics.clicked(partnerSettings)
                echoMetrics.totalClicks should be (num)
                echoMetrics.clicks should be (num)
                echoMetrics.credit should be (2f)
                echoMetrics.fee should be (1.5f)
            }

            verifyZeroResidual(echoMetrics)
        }


        it("should calculate the residual fee up to max echoed percentage when clicked") {
            given("an Echo")
            when("clicked multiple times outside credit window")
            then("it should calculate the residual fee up to max echoed percentage")
            partnerSettings = partnerSettings.copy(echoedMaxPercentage = 0.15f)
            residualEchoMetrics = residualEchoMetrics.copy(
                    totalClicks = 0,
                    clicks = 0,
                    credit = 0,
                    fee = 0,
                    residualClicks = 0,
                    residualCredit = 0,
                    residualFee = 0,
                    creditWindowEndsAt = null)

            for (num <- residualEchoMetrics.totalClicks + 1 to partnerSettings.maxClicks)
                residualEchoMetrics = residualEchoMetrics.clicked(partnerSettings)

            residualEchoMetrics.totalClicks should equal (partnerSettings.maxClicks)
            residualEchoMetrics.residualClicks should equal (residualEchoMetrics.totalClicks)
            residualEchoMetrics.residualCredit should be (2f)
            residualEchoMetrics.residualFee should be (1.5f)

            for (num <- residualEchoMetrics.totalClicks + 1 to (residualEchoMetrics.totalClicks + 5)) {
                residualEchoMetrics = residualEchoMetrics.clicked(partnerSettings)
                residualEchoMetrics.totalClicks should be (num)
                residualEchoMetrics.residualClicks should be (num)
                residualEchoMetrics.residualCredit should be (2f)
                residualEchoMetrics.residualFee should be (1.5f)
            }

            verifyZero(residualEchoMetrics)
        }


        it("should calculate the fee using echoed match percentage up to echoed max percentage when clicked") {
            given("an Echo")
            when("clicked multiple times")
            then("it should calculate the fee using echoed match percentage up to echoed max percentage ")
            partnerSettings = partnerSettings.copy(echoedMaxPercentage = 0.18f, echoedMatchPercentage = 0.8f)
            echoMetrics = echoMetrics.copy(totalClicks = 0, clicks = 0, credit = 0, fee = 0, creditWindowEndsAt = null)

            echoMetrics = echoMetrics.echoed(partnerSettings)
            echoMetrics.totalClicks should be (0)
            echoMetrics.clicks should be (0)
            echoMetrics.credit should be (0.1f plusOrMinus 0.01f)
            echoMetrics.fee should be (0.08f plusOrMinus 0.001f)


            echoMetrics = echoMetrics.clicked(partnerSettings).clicked(partnerSettings) //click to get it past the min percentage
            echoMetrics.totalClicks should be (2)
            echoMetrics.clicks should be (2)

            var clicks = echoMetrics.clicks
            var credit = echoMetrics.credit
            var fee = echoMetrics.fee

            fee should be < (credit)

            for (num <- echoMetrics.totalClicks + 1 to partnerSettings.maxClicks) {
                echoMetrics = echoMetrics.clicked(partnerSettings)
                echoMetrics.totalClicks should be (num)
                echoMetrics.clicks should be (num)
                echoMetrics.credit should be > credit
                echoMetrics.fee should be < echoMetrics.credit
                credit = echoMetrics.credit
                fee = echoMetrics.fee
                clicks = echoMetrics.clicks
            }

            echoMetrics.totalClicks should equal (partnerSettings.maxClicks)
            echoMetrics.clicks should equal (echoMetrics.totalClicks)
            echoMetrics.credit should be (2f)
            echoMetrics.fee should be (1.8f plusOrMinus 0.01f)

            verifyZeroResidual(echoMetrics)

            for (num <- echoMetrics.totalClicks + 1 to (echoMetrics.totalClicks + 5)) {
                echoMetrics = echoMetrics.clicked(partnerSettings)
                echoMetrics.totalClicks should be (num)
                echoMetrics.clicks should be (num)
                echoMetrics.credit should be (2f)
                echoMetrics.fee should be (1.8f plusOrMinus 0.01f)
            }

            verifyZeroResidual(echoMetrics)
        }


        it("should calculate the residual fee using echoed match percentage up to echoed max percentage when clicked") {
            given("an Echo")
            when("clicked multiple times outside of credit window")
            then("it should calculate the residual fee using echoed match percentage up to echoed max percentage ")
            partnerSettings = partnerSettings.copy(echoedMaxPercentage = 0.18f, echoedMatchPercentage = 0.8f)
            residualEchoMetrics = residualEchoMetrics.copy(
                    totalClicks = 0,
                    clicks = 0,
                    credit = 0,
                    fee = 0,
                    residualClicks = 0,
                    residualCredit = 0,
                    residualFee = 0,
                    creditWindowEndsAt = null)

            residualEchoMetrics = residualEchoMetrics.clicked(partnerSettings)
            residualEchoMetrics.totalClicks should be (1)
            residualEchoMetrics.residualClicks should be (1)
            residualEchoMetrics.residualCredit should be (0.1f plusOrMinus 0.01f)
            residualEchoMetrics.residualFee should be (0.08f plusOrMinus 0.001f)

            verifyZero(residualEchoMetrics)

            residualEchoMetrics = residualEchoMetrics.clicked(partnerSettings).clicked(partnerSettings) //click to get it past the min percentage
            residualEchoMetrics.totalClicks should be (3)
            residualEchoMetrics.residualClicks should be (3)

            var residualClicks = residualEchoMetrics.residualClicks
            var residualCredit = residualEchoMetrics.residualCredit
            var residualFee = residualEchoMetrics.residualFee

            residualFee should be < (residualCredit)

            for (num <- residualEchoMetrics.totalClicks + 1 to partnerSettings.maxClicks) {
                residualEchoMetrics = residualEchoMetrics.clicked(partnerSettings)
                residualEchoMetrics.totalClicks should be (num)
                residualEchoMetrics.residualClicks should be (num)
                residualEchoMetrics.residualCredit should be > residualCredit
                residualEchoMetrics.residualFee should be < residualEchoMetrics.residualCredit
                residualCredit = residualEchoMetrics.residualCredit
                residualFee = residualEchoMetrics.residualFee
                residualClicks = residualEchoMetrics.residualClicks
            }

            residualEchoMetrics.totalClicks should equal (partnerSettings.maxClicks)
            residualEchoMetrics.residualClicks should equal (residualEchoMetrics.totalClicks)
            residualEchoMetrics.residualCredit should be (2f)
            residualEchoMetrics.residualFee should be (1.8f plusOrMinus 0.01f)

            verifyZero(residualEchoMetrics)

            for (num <- residualEchoMetrics.totalClicks + 1 to (residualEchoMetrics.totalClicks + 5)) {
                residualEchoMetrics = residualEchoMetrics.clicked(partnerSettings)
                residualEchoMetrics.totalClicks should be (num)
                residualEchoMetrics.residualClicks should be (num)
                residualEchoMetrics.residualCredit should be (2f)
                residualEchoMetrics.residualFee should be (1.8f plusOrMinus 0.01f)
            }

            verifyZero(residualEchoMetrics)
        }


        it("should calculate the credit and fee only within the credit window") {
            given("an Echo")
            when("clicked multiple times within the credit window")
            then("it should calculate the credit and fee only when in the credit window")
            partnerSettings = partnerSettings.copy(echoedMaxPercentage = 0.2f, echoedMatchPercentage = 1f)
            echoMetrics = echoMetrics.copy(totalClicks = 0, clicks = 0, credit = 0, fee = 0, creditWindowEndsAt = null)

            verifyZero(echoMetrics)
            verifyZeroResidual(echoMetrics)

            echoMetrics = echoMetrics.clicked(partnerSettings).clicked(partnerSettings) //click to get it past the min percentage
            echoMetrics.totalClicks should be (2)
            echoMetrics.residualClicks should be (2)
            echoMetrics.residualCredit should be (1)
            echoMetrics.residualFee should be (1)

            verifyZero(echoMetrics)

            echoMetrics = echoMetrics.echoed(partnerSettings)
            echoMetrics.totalClicks should be (2)
            echoMetrics.credit should be (0.1f plusOrMinus 0.01f)
            echoMetrics.fee should be (0.1f plusOrMinus 0.01f)

            echoMetrics = echoMetrics.clicked(partnerSettings).clicked(partnerSettings) //click to get it past the min percentage
            echoMetrics.totalClicks should be (4)
            echoMetrics.clicks should be (2)
            echoMetrics.credit should be (1.1f plusOrMinus 0.01f)
            echoMetrics.fee should be (1.1f plusOrMinus 0.01f)

            echoMetrics.totalClicks should equal(echoMetrics.clicks + echoMetrics.residualClicks)
//            echoMetrics.credit should equal(echoMetrics.residualCredit)
//            echoMetrics.fee should equal(echoMetrics.residualFee)

            var clicks = echoMetrics.clicks
            var credit = echoMetrics.credit
            var fee = echoMetrics.fee

            for (num <- echoMetrics.totalClicks + 1 to (partnerSettings.maxClicks - 5)) {
                echoMetrics = echoMetrics.clicked(partnerSettings)
                echoMetrics.totalClicks should be (num)
                echoMetrics.clicks should be (echoMetrics.totalClicks - echoMetrics.residualClicks)
                echoMetrics.credit should be > credit
                echoMetrics.fee should be > fee
                credit = echoMetrics.credit
                fee = echoMetrics.fee
                clicks = echoMetrics.clicks
            }

            echoMetrics.clicks should equal (clicks)
            echoMetrics.credit should be (1.21f plusOrMinus 0.01f)
            echoMetrics.fee should be (1.21f plusOrMinus 0.01f)

            echoMetrics.totalClicks should equal(echoMetrics.clicks + echoMetrics.residualClicks)

            echoMetrics.residualClicks should be (2)
            echoMetrics.residualCredit should be (1)
            echoMetrics.residualFee should be (1)

            var residualCredit = echoMetrics.residualCredit
            var residualFee = echoMetrics.residualFee
            var residualClicks = echoMetrics.residualClicks

            for (num <- echoMetrics.totalClicks + 1 to partnerSettings.maxClicks) {
                echoMetrics = echoMetrics.clicked(partnerSettings, false) //clicks no longer within window...
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

            echoMetrics.totalClicks should equal(echoMetrics.clicks + echoMetrics.residualClicks)
        }

    }
}
