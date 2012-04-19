package com.echoed.chamber

import org.scalatest.Suites
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.echoed.chamber.services.facebook.FacebookAccessActorTest
import com.echoed.chamber.domain._

@RunWith(classOf[JUnitRunner])
class ChamberTestSuite extends Suites(
    new EchoTest,
    new EchoMetricsTest,
    new PartnerUserTest,
    new PartnerSettingsTest,
    new ImageTest,
    new FacebookAccessActorTest)

