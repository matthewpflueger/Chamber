package com.echoed.chamber.domain

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, Spec}
import org.scalatest.matchers.ShouldMatchers
import java.util.Date


@RunWith(classOf[JUnitRunner])
class EchoTest extends Spec with GivenWhenThen with ShouldMatchers {

    describe("An Echo") {

        it("should return null for the id if missing properties") {
            given("a newly instantiated Echo")
            val echo = Echo.make(null, null, null, null, "step", null, 0, null, null, null, null, null, null, null)

            when("no properties have been set")
            then("the id should be null")
            echo.echoPossibilityId should be (null)
        }

        it("should return a url safe base 64 string when all necessary properties are set") {
            given("an Echo with all necessary properties set")
            val echo = Echo.make(
                "retailerId",
                "customerId",
                "productId",
                new Date,
                "test",
                "orderId",
                100,
                "imageUrl",
                "landingPageUrl",
                "productName",
                "category",
                "brand",
                "description",
                "echoClickId")

            when("all necessary properties have been set")
            then("the id should return a valid base64 string")
            echo.echoPossibilityId should not be (null)
        }

        it("should return a map of its properties") {
            given("an Echo")
            val echo = Echo.make(
                "retailerId",
                "customerId",
                "productId",
                new Date,
                "test",
                "orderId",
                100,
                "imageUrl",
                "landingPageUrl",
                "productName",
                "category",
                "brand",
                "description",
                "echoClickId")

            when("asMap is called")
            val map = echo.asMap

            then("a map of all its properties should be returned")
            map.get("retailerId").get should equal ("retailerId")
        }

        it("should return a url string of its properties") {
            given("an Echo")
            val echo = Echo.make(
                "retailerId",
                "customerId",
                "productId",
                new Date,
                "test",
                "orderId",
                100,
                "imageUrl",
                "landingPageUrl",
                "productName",
                "category",
                "brand",
                "description",
                "echoClickId")

            when("asUrlParams is called")
            val urlParams = echo.asUrlParams()

            then("a string of all its properties should be returned in the form of url parameters")
            urlParams should not be (null)
        }
    }
}