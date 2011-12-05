package com.echoed.chamber.domain

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, Spec}
import org.scalatest.matchers.ShouldMatchers
import java.util.{UUID, Date}


@RunWith(classOf[JUnitRunner])
class RetailerUserTest extends Spec with GivenWhenThen with ShouldMatchers {

    val retailerUser = new RetailerUser(
        UUID.randomUUID.toString,
        "Test RetailerUser",
        "TestReatilerUser@echeod.com"
    )

    val password = "mytestpassword"

    describe("a RetailerUser") {

        it("should return a new RetailerUser on password creation") {
            given("a RetailerUser with no password set")
            retailerUser.password should be (null)

            when("given the initial password")
            then("return a new RetailerUser with the password")
            val newRetailerUser = retailerUser.createPassword(password)
            newRetailerUser should not be (null)
            newRetailerUser.password should not be (null)
            newRetailerUser should not equal(retailerUser)
            newRetailerUser.password should not equal(retailerUser.password)
            newRetailerUser.id should equal (retailerUser.id)
            newRetailerUser.salt should equal (retailerUser.salt)
        }

        it("should never have the same password as another RetailerUser") {
            given("a RetailerUser with a password")
            val oneRetailerUser = retailerUser.createPassword(password)

            when("another RetailerUser with the same plain text password")
            val twoRetailerUser = new RetailerUser(
                    retailerUser.retailerId,
                    retailerUser.name,
                    retailerUser.email).createPassword(password)

            then("the two RetailerUsers should still not have the same hashed password")
            oneRetailerUser.password should not equal (twoRetailerUser.password)
        }

        it("is capable of checking if a given plain text password is the same as its hashed password") {
            given("a RetailerUser with a password")
            val oneRetailerUser = retailerUser.createPassword(password)

            when("given the same password in plain text")
            then("it should return true")
            oneRetailerUser.isPassword(password) should be (true)

            when("given a different password in plain text")
            then("it should return false")
            oneRetailerUser.isPassword(password + "wrong") should be (false)
        }
    }
}
