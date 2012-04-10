package com.echoed.chamber.domain

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, Spec}
import org.scalatest.matchers.ShouldMatchers
import java.util.Date


@RunWith(classOf[JUnitRunner])
class EchoTest extends Spec with GivenWhenThen with ShouldMatchers {

    def makeEcho = {
        Echo.make(
            retailerId = "retailerId",
            customerId = "customerId",
            productId = "productId",
            boughtOn = new Date,
            step = "test",
            orderId = "orderId",
            price = 100,
            imageUrl = "imageUrl",
            landingPageUrl = "landingPageUrl",
            productName = "productName",
            category = "category",
            brand = "brand",
            description = "description",
            browserId = null,
            ipAddress = null,
            userAgent = null,
            referrerUrl = null,
            echoClickId = "echoClickId")
    }

    describe("An Echo") {

        it("should return null for the id if missing properties") {
            given("a newly instantiated Echo")
            val echo = Echo.make(
                retailerId = null,
                customerId = null,
                productId = null,
                boughtOn = new Date,
                step = "test",
                orderId = null,
                price = 100,
                imageUrl = "imageUrl",
                landingPageUrl = "landingPageUrl",
                productName = "productName",
                category = "category",
                brand = "brand",
                description = "description",
                browserId = null,
                ipAddress = null,
                userAgent = null,
                referrerUrl = null,
                echoClickId = "echoClickId")

            when("no properties have been set")
            then("the id should be null")
            echo.echoPossibilityId should be (null)
        }

        it("should return a url safe base 64 string when all necessary properties are set") {
            given("an Echo with all necessary properties set")
            val echo = makeEcho

            when("all necessary properties have been set")
            then("the id should return a valid base64 string")
            echo.echoPossibilityId should not be (null)
        }

        it("should return a map of its properties") {
            given("an Echo")
            val echo = makeEcho

            when("asMap is called")
            val map = echo.asMap

            then("a map of all its properties should be returned")
            map.get("retailerId").get should equal ("retailerId")
        }

        it("should return a url string of its properties") {
            given("an Echo")
            val echo = makeEcho

            when("asUrlParams is called")
            val urlParams = echo.asUrlParams()

            then("a string of all its properties should be returned in the form of url parameters")
            urlParams should not be (null)
        }
    }
}
