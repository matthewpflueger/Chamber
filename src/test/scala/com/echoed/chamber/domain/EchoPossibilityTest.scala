package com.echoed.chamber.domain

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, Spec}
import org.scalatest.matchers.ShouldMatchers
import java.util.Date


@RunWith(classOf[JUnitRunner])
class EchoPossibilityTest extends Spec with GivenWhenThen with ShouldMatchers {

    describe("An EchoPossibility") {

        it("should return null for the id if missing properties") {
            given("a newly instantiated EchoPossibility")
            val echoPossibility = new EchoPossibility(null, null, null, null, null, null, 0, null, null, null, null)

            when("no properties have been set")
            then("the id should be null")
            echoPossibility.id should be (null)
        }

        it("should return a url safe base 64 string when all necessary properties are set") {
            given("an EchoPossibility with all necessary properties set")
            val echoPossibility = new EchoPossibility(
                "retailerId",
                "customerId",
                "productId",
                new Date,
                null,
                "orderId",
                100,
                "imageUrl",
                null,
                null,
                "landingPageUrl")

            when("all necessary properties have been set")
            then("the id should return a valid base64 string")
            echoPossibility.id should not be (null)
        }

        it("should return a map of its properties") {
            given("an EchoPossibility")
            val echoPossibility = new EchoPossibility(
                "retailerId",
                "customerId",
                "productId",
                new Date,
                null,
                "orderId",
                100,
                "imageUrl",
                null,
                null,
                "landingPageUrl")

            when("asMap is called")
            val map = echoPossibility.asMap

            then("a map of all its properties should be returned")
            map.get("retailerId").get should equal ("retailerId")
        }

        it("should return a url string of its properties") {
            given("an EchoPossibility")
            val echoPossibility = new EchoPossibility(
                "retailerId",
                "customerId",
                "productId",
                new Date,
                null,
                "orderId",
                100,
                "imageUrl",
                null,
                null,
                "landingPageUrl")

            when("asUrlParams is called")
            val urlParams = echoPossibility.asUrlParams()

            then("a string of all its properties should be returned in the form of url parameters")
            urlParams should not be (null)
        }
    }
}
