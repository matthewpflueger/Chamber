package com.echoed.chamber

import org.scalatest.Suites
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.echoed.chamber.dao.DatabaseSuite

@RunWith(classOf[JUnitRunner])
class EchoedSiteSuite extends Suites(
    new DatabaseSuite,

    new ClosetExhibitIT,
    
    new EchoButtonIT,
    new FacebookLoginIT,
    new EchoIT,
    new TwitterLoginIT,
    new ClosetLoginIT)

