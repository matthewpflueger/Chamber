package com.echoed

import org.scalatest.Suites
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.echoed.chamber.{ChamberITSuite, ChamberTestSuite}
import com.echoed.util.EncrypterTest

@RunWith(classOf[JUnitRunner])
class EchoedSuite extends Suites(
    new EncrypterTest,
    new ChamberTestSuite,
    new ChamberITSuite)

