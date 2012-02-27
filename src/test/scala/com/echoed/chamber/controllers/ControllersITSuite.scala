package com.echoed.chamber.controllers

import org.scalatest.Suites
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.echoed.chamber.dao.DaoITSuite

@RunWith(classOf[JUnitRunner])
class ControllersITSuite extends Suites(
    new TwitterLoginIT,
    new FacebookLoginIT,
    new ClosetLoginIT,

    new FacebookAddIT,
    new TwitterAddIT,

    new EchoRequestIT,
    new EchoButtonIT,
    new EchoIT,
    new ClosetExhibitIT,
    new ClosetFriendsIT,

    new FacebookAppIT,

    new PartnerUserLoginIT)

