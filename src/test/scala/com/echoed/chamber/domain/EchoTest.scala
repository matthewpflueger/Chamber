package com.echoed.chamber.domain

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, Spec}
import org.scalatest.matchers.ShouldMatchers
import java.util.Date


@RunWith(classOf[JUnitRunner])
class EchoTest extends Spec with GivenWhenThen with ShouldMatchers {

    var retailerSettings = new RetailerSettings(
            retailerId = "retailerId",
            closetPercentage = 0.01f,
            minClicks = 1,
            minPercentage = 0.1f,
            maxClicks = 10,
            maxPercentage = 0.2f,
            echoedMatchPercentage = 1f,
            echoedMaxPercentage = 0.2f,
            activeOn = new Date)

    var echo = new Echo(
            retailerId = "retailerId",
            customerId = "customerId",
            productId = "productId",
            boughtOn = new Date,
            orderId = "orderId",
            price = 10.00f,
            imageUrl = "imageUrl",
            echoedUserId = "echoedUserId",
            facebookPostId = "facebookPostId",
            twitterStatusId = "twitterStatusId",
            echoPossibilityId = "echoPossibilityId",
            landingPageUrl = "landingPageUrl",
            productName= "My Awesome Boots",
            category= "Footwear",
            brand = "Nike",
            retailerSettingsId = retailerSettings.id)

    describe("An Echo") {

        it("should calculate the credit and fee for being echoed") {
            given("a new Echo")
            when("not echoed before")
            then("it should calculate the credit and fee when echoed")
            echo = echo.echoed(retailerSettings)
            echo.totalClicks should be (0)
            echo.credit should be (0.1f plusOrMinus 0.01f)
            echo.fee should be (0.1f plusOrMinus 0.01f)
        }

        it("cannot be echoed twice") {
            given("an already echoed Echo")
            when("echoed again")
            then("it should throw an exception")
            evaluating { echo.echoed(retailerSettings) } should produce [IllegalStateException]
        }

        it("should calculate the credit and fee for being clicked") {
            given("an Echo")
            when("clicked")
            then("it should calculate the credit and fee of the click")
            echo = echo.clicked(retailerSettings)
            echo.totalClicks should be (1)
            echo.credit should be (1)
            echo.fee should be (1)
        }

        it("should calculate the credit and fee up to max clicks when clicked") {
            given("an Echo")
            when("clicked multiple times")
            then("it should calculate the credit and fee of the click up to max clicks")
            echo.totalClicks should be (1)
            echo.credit should be (1)
            echo.fee should be (1)


            echo = echo.clicked(retailerSettings) //click once to get it past the min percentage
            echo.totalClicks should be (2)

            var credit = echo.credit
            var fee = echo.fee

            for ( num <- echo.totalClicks + 1 to retailerSettings.maxClicks ) {
                echo = echo.clicked(retailerSettings)
                echo.totalClicks should be (num)
                echo.credit should be > credit
                echo.fee should be > fee
                credit = echo.credit
                fee = echo.fee
            }

            echo.totalClicks should equal (retailerSettings.maxClicks)
            echo.credit should be (2f)
            echo.fee should be (2f)

            for ( num <- echo.totalClicks + 1 to (echo.totalClicks + 5)) {
                echo = echo.clicked(retailerSettings)
                echo.totalClicks should be (num)
                echo.credit should be (2f)
                echo.fee should be (2f)
            }
        }

        it("should calculate the fee up to max echoed percentage when clicked") {
            given("an Echo")
            when("clicked multiple times")
            then("it should calculate the fee up to max echoed percentage")
            retailerSettings = retailerSettings.copy(echoedMaxPercentage = 0.15f)
            echo = echo.copy(totalClicks = 0, credit = 0, fee = 0)

            echo = echo.echoed(retailerSettings)

            for ( num <- echo.totalClicks + 1 to retailerSettings.maxClicks ) echo = echo.clicked(retailerSettings)

            echo.totalClicks should equal (retailerSettings.maxClicks)
            echo.credit should be (2f)
            echo.fee should be (1.5f)

            for ( num <- echo.totalClicks + 1 to (echo.totalClicks + 5)) {
                echo = echo.clicked(retailerSettings)
                echo.totalClicks should be (num)
                echo.credit should be (2f)
                echo.fee should be (1.5f)
            }
        }

        it("should calculate the fee using echoed match percentage up to echoed max percentage when clicked") {
            given("an Echo")
            when("clicked multiple times")
            then("it should calculate the fee using echoed match percentage up to echoed max percentage ")
            retailerSettings = retailerSettings.copy(echoedMaxPercentage = 0.18f, echoedMatchPercentage = 0.8f)
            echo = echo.copy(totalClicks = 0, credit = 0, fee = 0)

            echo = echo.echoed(retailerSettings)
            echo.totalClicks should be (0)
            echo.credit should be (0.1f plusOrMinus 0.01f)
            echo.fee should be (0.08f plusOrMinus 0.001f)


            echo = echo.clicked(retailerSettings).clicked(retailerSettings) //click to get it past the min percentage
            echo.totalClicks should be (2)

            var credit = echo.credit
            var fee = echo.fee

            fee should be < (credit)

            for ( num <- echo.totalClicks + 1 to retailerSettings.maxClicks ) {
                echo = echo.clicked(retailerSettings)
                echo.totalClicks should be (num)
                echo.credit should be > credit
                echo.fee should be < echo.credit
                credit = echo.credit
                fee = echo.fee
            }

            echo.totalClicks should equal (retailerSettings.maxClicks)
            echo.credit should be (2f)
            echo.fee should be (1.8f plusOrMinus 0.01f)

            for ( num <- echo.totalClicks + 1 to (echo.totalClicks + 5)) {
                echo = echo.clicked(retailerSettings)
                echo.totalClicks should be (num)
                echo.credit should be (2f)
                echo.fee should be (1.8f plusOrMinus 0.01f)
            }
        }

    }
}
