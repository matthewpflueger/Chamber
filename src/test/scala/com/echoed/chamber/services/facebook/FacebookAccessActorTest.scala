package com.echoed.chamber.services.facebook

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{Spec, GivenWhenThen}
import java.util.Properties
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import akka.testkit.TestActorRef

@RunWith(classOf[JUnitRunner])
class FacebookAccessActorTest extends Spec with GivenWhenThen with ShouldMatchers {

    describe("A FacebookAccessActor") {

        it("should not start if missing properties") {
            given("an empty Properties object set on a FacebookAccessActor")
            val actorRef = TestActorRef[FacebookAccessActor]
            actorRef.underlyingActor.properties = new Properties

            when("the actor is started")
            then("the actor should not start")
            and("an AssertionError will have been thrown")
            evaluating {
                actorRef.start
            } should produce [AssertionError]
            actorRef.isUnstarted should be (true)
        }

    }
}
