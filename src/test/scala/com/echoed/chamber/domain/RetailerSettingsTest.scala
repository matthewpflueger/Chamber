package com.echoed.chamber.domain

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, Spec}
import org.scalatest.matchers.ShouldMatchers
import java.util.Date


@RunWith(classOf[JUnitRunner])
class RetailerSettingsTest extends Spec with GivenWhenThen with ShouldMatchers {

    describe("A RetailerSettings") {

        it("cannot be constructed with invalid values") {

            given("a RetailerSettings")
            when("everything is negative")
            then("throw IllegalArgumentException")
            evaluating { new RetailerSettings(
                retailerId = "retailerId",
                closetPercentage = -1f,
                minClicks = -1,
                minPercentage = -1f,
                maxClicks = -1,
                maxPercentage = -1f,
                echoedMaxPercentage = -1f,
                echoedMatchPercentage = -1f,
                activeOn = new Date)
            } should produce [IllegalArgumentException]

            given("a RetailerSettings")
            when("minClicks is greater than maxClicks")
            then("throw IllegalArgumentException")
            evaluating { new RetailerSettings(
                retailerId = "retailerId",
                closetPercentage = 0,
                minClicks = 1,
                minPercentage = 0,
                maxClicks = 0,
                maxPercentage = 0,
                echoedMaxPercentage = 0,
                echoedMatchPercentage = 0,
                activeOn = new Date)
            } should produce [IllegalArgumentException]


            given("a RetailerSettings")
            when("minPercentage is less than closetPercentage")
            then("throw IllegalArgumentException")
            evaluating { new RetailerSettings(
                retailerId = "retailerId",
                closetPercentage = 0.1f,
                minClicks = 0,
                minPercentage = 0.01f,
                maxClicks = 0,
                maxPercentage = 0,
                echoedMaxPercentage = 0,
                echoedMatchPercentage = 0,
                activeOn = new Date)
            } should produce [IllegalArgumentException]


            given("a RetailerSettings")
            when("maxPercentage is less than minPercentage")
            then("throw IllegalArgumentException")
            evaluating { new RetailerSettings(
                retailerId = "retailerId",
                closetPercentage = 0,
                minClicks = 0,
                minPercentage = 0.1f,
                maxClicks = 0,
                maxPercentage = 0.01f,
                echoedMaxPercentage = 0,
                echoedMatchPercentage = 0,
                activeOn = new Date)
            } should produce [IllegalArgumentException]

        }
    }
}
