package com.echoed.chamber.dao.views

import com.echoed.chamber.domain.views.{StoryFull, Feed, EchoViewPublic}
import java.util.{List => JList}
import org.apache.ibatis.annotations.Param
import com.echoed.chamber.domain.Tag


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

    def findStoryByEchoedUserId(
            @Param("echoedUserId") echoedUserId: String,
            @Param("start") start: Int,
            @Param("limit") limit: Int): JList[StoryFull]

    def findStoryByPartnerId(
            @Param("partnerId") partnerId: String,
            @Param("start") start: Int,
            @Param("limit") limit: Int): JList[StoryFull]

    def getStories(
            @Param("start") start: Int,
            @Param("limit") limit: Int): JList[StoryFull]

    def getAllStories: JList[StoryFull]

    def getStoryIds: Array[String]

    def findTags(
            @Param("id") id: String): JList[Tag]

    def getPartnerIds: Array[String]

}