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
            val echoPossibility = new EchoPossibility

            when("no properties have been set")
            then("the id should be null")
            echoPossibility.id should be (null)
        }

        it("should return a url safe base 64 string when all necessary properties are set") {
            given("an EchoPossibility with all necessary properties set")
            val (echoPossibility, expectedBase64Value) = EchoPossibilityHelper.getValidEchoPossibilityAndHash()

            when("all necessary properties have been set")
            then("the id should return a valid base64 string")
            echoPossibility.id should equal (expectedBase64Value)
        }
    }
}

object EchoPossibilityHelper {
    def getValidEchoPossibilityAndHash(
            step: String = "button",
            echoedUserId: String = null,
            //a normal base64 will have one or more '=' characters for padding - they are ripped off for url safe base64 strings...
            expectedEchoPossibilityId: String = "dGVzdFJldGFpbGVySWR0ZXN0UmV0YWlsZXJDdXN0b21lcklkdGVzdFByb2R1Y3RJZFdlZCBOb3YgMDkgMTU6MzY6NTYgRVNUIDIwMTE") = {

        (new EchoPossibility(
                expectedEchoPossibilityId,
                "testRetailerId",
                "testRetailerCustomerId",
                "testProductId",
                new Date(1320871016126L), //Wed Nov 09 15:36:56 EST 2011
                step,
                echoedUserId
                ),

        expectedEchoPossibilityId)
    }
}