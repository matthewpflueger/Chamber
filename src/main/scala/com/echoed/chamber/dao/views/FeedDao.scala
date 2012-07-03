package com.echoed.chamber.dao.views

import com.echoed.chamber.domain.views.{StoryFull, Feed, EchoViewPublic}
import java.util.{List => JList}
import org.apache.ibatis.annotations.Param


trait FeedDao {

    def findByEchoedUserId(
            @Param("id") echoedUserId: String,
            @Param("start") start: Int,
            @Param("limit") limit: Int): Feed

    def getEchoedUserFeed(
            @Param("echoedUserId") echoedUserId: String,
            @Param("start") start: Int,
            @Param("limit") limit: Int): JList[EchoViewPublic]

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

    def findStoryById(storyId: String): StoryFull

    def findStoryByEchoId(echoId: String): StoryFull

    def findStoryByEchoedUserId(echoedUserId: String): JList[StoryFull]

    def findStoryByPartnerId(partnerId: String): JList[StoryFull]

    def getStories(
            @Param("start") start: Int,
            @Param("limit") limit: Int): JList[StoryFull]

    def getStoryIds: Array[String]

    def getPartnerIds: Array[String]

}