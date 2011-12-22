package com.echoed.chamber

import org.scalatest.Suites
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.echoed.chamber.dao.DaoITSuite
import com.echoed.chamber.controllers.ControllersITSuite
import com.echoed.chamber.services.facebook.FacebookPostCrawlerActorIT

@RunWith(classOf[JUnitRunner])
class ChamberITSuite extends Suites(
    new FacebookPostCrawlerActorIT,
    new DaoITSuite,
    new ControllersITSuite)


