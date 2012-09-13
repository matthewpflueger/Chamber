package com.echoed.chamber.dao.views

import com.echoed.chamber.domain.views.{StoryFull, Feed}
import java.util.{List => JList}
import org.apache.ibatis.annotations.Param


trait FeedDao {

    def findByEchoedUserId(
            @Param("id") echoedUserId: String,
            @Param("start") start: Int,
            @Param("limit") limit: Int): Feed

    def getPartnerIds: Array[String]

}