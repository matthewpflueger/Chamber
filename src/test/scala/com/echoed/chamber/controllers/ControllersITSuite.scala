package com.echoed.chamber.controllers

import org.scalatest.Suites
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.echoed.chamber.dao.DaoITSuite

@RunWith(classOf[JUnitRunner])
class ControllersITSuite extends Suites(
    new ClosetExhibitIT,
    new ClosetFriendsIT,
    new EchoButtonIT,
    new FacebookLoginIT,
    new FacebookAppIT,
    new FacebookAddIT,
    new EchoIT,
    new TwitterLoginIT,
    new TwitterAddIT,
    new ClosetLoginIT,
    new PartnerUserLoginIT)

