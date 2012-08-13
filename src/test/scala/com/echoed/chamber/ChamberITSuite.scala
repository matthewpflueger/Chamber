package com.echoed.chamber

import org.scalatest.Suites
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.echoed.chamber.services.facebook.FacebookPostCrawlerActorIT
import com.echoed.chamber.services.email.EmailServiceActorIT
import services.image.ImageServiceActorIT

@RunWith(classOf[JUnitRunner])
class ChamberITSuite extends Suites(
    new FacebookPostCrawlerActorIT,
    new EmailServiceActorIT,
    new ImageServiceActorIT)


