package com.echoed.chamber.dao

import com.echoed.chamber.domain.FacebookPost
import java.util.{Date, List => JList}
import org.apache.ibatis.annotations.Param
import com.echoed.chamber.domain.views.FacebookPostData


trait FacebookPostDao {

    def findByEchoId(echoId: String): FacebookPost

    def findByEchoedUserId(echoedUserId: String): JList[FacebookPost]

//    def findPostToCrawl(
//            @Param("postedOnStartDate") postedOnStartDate: Date,
//            @Param("postedOnEndDate") postedOnEndDate: Date,
//            @Param("crawledOnEndDate") crawledOnEndDate: Date): FacebookPostData

    def findPostToCrawl(
            @Param("postedOnStartDate") postedOnStartDate: Date,
            @Param("postedOnEndDate") postedOnEndDate: Date,
            @Param("crawledOnEndDate") crawledOnEndDate: Date,
            @Param("echoedUserId") echoedUserId: String): FacebookPostData

//    def findOldPostToCrawl(
//            @Param("postedOnBeforeDate") postedOnBeforeDate: Date,
//            @Param("crawledOnEndDate") crawledOnEndDate: Date): FacebookPostData

    def findOldPostToCrawl(
            @Param("postedOnBeforeDate") postedOnBeforeDate: Date,
            @Param("crawledOnEndDate") crawledOnEndDate: Date,
            @Param("echoedUserId") echoedUserId: String): FacebookPostData

    def insert(facebookPost: FacebookPost): Int

    def updatePostedOn(facebookPost: FacebookPost): Int

    def updatePostForCrawl(facebookPost: FacebookPost): Int

    def resetPostsToCrawl(facebookUserId: String): Int

    def deleteByEchoedUserId(echoedUserId: String): Int

}
