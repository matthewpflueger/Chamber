package com.echoed.chamber

import org.scalatest.Suites


class EchoedSiteSuiteIT extends Suites(
    new EchoButtonIT,
    new EchoIT)
