package com.echoed.chamber

import org.scalatest.Suites
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.echoed.chamber.dao.DaoITSuite
import com.echoed.chamber.services.facebook.FacebookAccessActorTest
import com.echoed.chamber.domain.{RetailerSettingsTest, RetailerUserTest, EchoTest, EchoPossibilityTest}

@RunWith(classOf[JUnitRunner])
class ChamberTestSuite extends Suites(
    new EchoPossibilityTest,
    new EchoTest,
    new RetailerUserTest,
    new RetailerSettingsTest,
    new FacebookAccessActorTest)

