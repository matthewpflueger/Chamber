package com.echoed.chamber.dao

import com.echoed.chamber.domain.Story

import java.util.{List => JList}


trait StoryDao {

    def deleteByEchoedUserId(echoedUserId: String): Int

    def insert(story: Story): Int

    def findByEchoedUserId(echoedUserId: String): JList[Story]
}
