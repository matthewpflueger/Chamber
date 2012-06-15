package com.echoed.chamber.dao.views

import com.echoed.chamber.domain.views.{StoryFull, Feed, EchoViewPublic}
import java.util.{List => JList}
import org.apache.ibatis.annotations.Param


trait FeedDao {

    def findByEchoedUserId(
            @Param("id") echoedUserId: String,
            @Param("start") start: Int,
            @Param("limit") limit: Int): Feed

    def getPublicFeed(
            @Param("start")  start: Int,
            @Param("limit") limit: Int): JList[EchoViewPublic]

    def getCategoryFeed(
            @Param("categoryId") categoryId: String,
            @Param("start") start: Int,
            @Param("limit") limit: Int): JList[EchoViewPublic]

    def getPartnerFeed(
            @Param("partnerId") partnerId: String,
            @Param("start")  start: Int,
            @Param("limit") limit : Int): JList[EchoViewPublic]

    def getStory(storyId: String): StoryFull
}