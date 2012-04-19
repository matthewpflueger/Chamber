package com.echoed.chamber.domain

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, Spec}
import org.scalatest.matchers.ShouldMatchers
import java.util.{UUID, Date}


@RunWith(classOf[JUnitRunner])
class PartnerUserTest extends Spec with GivenWhenThen with ShouldMatchers {

    val partnerUser = new PartnerUser(
        UUID.randomUUID.toString,
        "Test PartnerUser",
        "TestReatilerUser@echeod.com"
    )

    val password = "mytestpassword"

    describe("a PartnerUser") {

        it("should return a new PartnerUser on password creation") {
            given("a PartnerUser with no password set")
            partnerUser.password should be (null)

            when("given the initial password")
            then("return a new PartnerUser with the password")
            val newPartnerUser = partnerUser.createPassword(password)
            newPartnerUser should not be (null)
            newPartnerUser.password should not be (null)
            newPartnerUser should not equal(partnerUser)
            newPartnerUser.password should not equal(partnerUser.password)
            newPartnerUser.id should equal (partnerUser.id)
            newPartnerUser.salt should equal (partnerUser.salt)
        }

        it("should never have the same password as another PartnerUser") {
            given("a PartnerUser with a password")
            val onePartnerUser = partnerUser.createPassword(password)

            when("another PartnerUser with the same plain text password")
            val twoPartnerUser = new PartnerUser(
                    partnerUser.partnerId,
                    partnerUser.name,
                    partnerUser.email).createPassword(password)

            then("the two PartnerUsers should still not have the same hashed password")
            onePartnerUser.password should not equal (twoPartnerUser.password)
        }

        it("is capable of checking if a given plain text password is the same as its hashed password") {
            given("a PartnerUser with a password")
            val onePartnerUser = partnerUser.createPassword(password)

            when("given the same password in plain text")
            then("it should return true")
            onePartnerUser.isPassword(password) should be (true)

            when("given a different password in plain text")
            then("it should return false")
            onePartnerUser.isPassword(password + "wrong") should be (false)
        }
    }
}
