package com.echoed.chamber.dao

import com.echoed.chamber.domain.Story

import java.util.{List => JList}
import org.apache.ibatis.annotations.Param


trait StoryDao {

    def deleteByEchoedUserId(echoedUserId: String): Int

    def insert(story: Story): Int

    def findByEchoedUserId(echoedUserId: String): JList[Story]

    def findByIdAndEchoedUserId(
            @Param("id") id: String,
            @Param("echoedUserId") echoedUserId: String): Story

    def update(story: Story): Int
}
