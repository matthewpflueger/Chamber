package com.echoed.chamber

import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith


@RunWith(classOf[JUnitRunner])
class EchoButtonIT extends FeatureSpec with GivenWhenThen {
    feature("An Echo button is shown on a retailer's purchase confirmation page") {

        info("As a recent purchaser")
        info("I want to be able to click on the Echo button")
        info("So that I can share my purchase with friends")
 
    scenario("button is requested with no retailer, customer, or purchase info") {
        given("a request for the button")
        when("there is no other information")
        then("redirect to the button")
        and("no info should be recorded in the database")
        pending
    }
 
  }
}