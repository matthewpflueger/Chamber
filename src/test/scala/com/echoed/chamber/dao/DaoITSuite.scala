package com.echoed.chamber.dao

import org.scalatest.Suites
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.echoed.chamber.dao.views.ClosetDaoIT

@RunWith(classOf[JUnitRunner])
class DaoITSuite extends Suites(
    new EchoClickDaoIT,
    new EchoDaoIT,
    new EchoedUserDaoIT,
    new EchoedFriendDaoIT,
    new FacebookFriendDaoIT,
    new FacebookPostDaoIT,
    new FacebookUserDaoIT,
    new FacebookTestUserDaoIT,
    new FacebookLikeDaoIT,
    new FacebookCommentDaoIT,
    new ImageDaoIT,
    new PartnerDaoIT,
    new PartnerSettingsDaoIT,
    new PartnerUserDaoIT,
    new TwitterFollowerDaoIT,
    new TwitterStatusDaoIT,
    new TwitterUserDaoIT,
    new ClosetDaoIT)


