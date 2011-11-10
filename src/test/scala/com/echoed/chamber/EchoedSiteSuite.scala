package com.echoed.chamber

import org.scalatest.Suites
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EchoedSiteSuite extends Suites(
    new EchoButtonIT,
    new EchoIT)
