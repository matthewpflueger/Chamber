package com.echoed.chamber.controllers

import org.scalatest.Suites
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.echoed.chamber.dao.DaoITSuite

@RunWith(classOf[JUnitRunner])
class ControllersITSuite extends Suites(
    new ClosetExhibitIT,
    new EchoButtonIT,
    new FacebookLoginIT,
    new EchoIT,
    new TwitterLoginIT,
    new ClosetLoginIT)

